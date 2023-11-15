package de.tautenhahn.easydata.docx;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import java.io.*;
import java.util.List;

/**
 * Manipulates the XML structure inside the DOCX so that each special token is guaranteed to occur within a single run.
 * In DOCX, consecutive characters of the content (even within one word) may be separated into different
 * runs (XML-Elements within DOCX-part). Thus, special tokens might not be caught by any regular expressions applied to
 * the XML content. On the other hand, EasyData relies on working directly on the XML stream which is necessary
 * to manipulate XML structure if needed. This class re-distributes content into runs to avoid that problem.
 * <br/>
 * As a work-around, you can simply save, re-open and save the document with Word or
 * Libreoffice and not use this class.
 */
public class DocxDefragmenter {

    private static final int MAX_TAG_LENGTH = 300;
    private static final char START = '(';
    private static final char MARKER = '@';
    private static final char END = ')';


    int changes;

    /**
     * Collects each special tag into a single run. Any content which does not conform to the convention
     * "opening character, marker character, stuff not containing opening or closing character, closing character"
     * is not changed here.
     */
    public void reorganize(InputStream ins, OutputStream out) throws IOException, InvalidFormatException {
        try (XWPFDocument doc = new XWPFDocument(OPCPackage.open(ins))) {
            reorganizeParagraphs(doc.getParagraphs());
            reorganizeTables(doc.getTables());
            doc.getFooterList().forEach(footer -> {
                reorganizeParagraphs(footer.getParagraphs());
                reorganizeTables(footer.getTables());
            });
            doc.getHeaderList().forEach(header -> {
                reorganizeParagraphs(header.getParagraphs());
                reorganizeTables(header.getTables());
            });
            doc.write(out);
        }
    }

    private void reorganizeTables(List<XWPFTable> tables) {
        tables.stream()
                .flatMap(table -> table.getRows().stream())
                .flatMap(row -> row.getTableCells().stream())
                .forEach(cell -> {
                    reorganizeParagraphs(cell.getParagraphs());
                    reorganizeTables(cell.getTables());
                });
    }

    private void reorganizeParagraphs(List<XWPFParagraph> paragraphs) {
        paragraphs.forEach(this::reorganizeParagraph);
    }

    /**
     * Changes the run contents within a paragraph. Note that the setText()-Method does not set the text of a run but
     * instead appends the given value. To set the value we must call setText(value, 0).
     *
     * @param paragraph has same content, but no special tag is split into more than one text node
     */
    void reorganizeParagraph(XWPFParagraph paragraph) {
        XWPFRun runToExtend = null;
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.text();
            if (runToExtend != null || text.indexOf(START) >= 0) {
                mergeTextNodes(run);
            }
            if (runToExtend != null) {
                int endPos = text.indexOf(END);
                if (cannotBeExtendedToValidTag(runToExtend.text(), text, endPos)) {
                    runToExtend = null;
                } else if (endPos > -1) {
                    runToExtend.setText(runToExtend.text() + text.substring(0, endPos + 1), 0);
                    text = text.substring(endPos + 1);
                    run.setText(text, 0);
                    changes++;
                    runToExtend = null;
                } else {
                    runToExtend.setText(runToExtend.text() + text, 0);
                    text = "";
                    run.setText(text, 0);
                    changes++;
                    continue;
                }
            }
            if (mayContainUnfinishedTag(text)) {
                runToExtend = run;
            }
        }
    }

    private boolean mayContainUnfinishedTag(String text) {
        int lastStartPos = text.lastIndexOf(START);
        int lastEndPos = text.lastIndexOf(END);
        return lastStartPos > -1 &&
                (lastStartPos == text.length() - 1 || text.charAt(lastStartPos + 1) == MARKER) &&
                lastEndPos < lastStartPos;
    }

    /**
     * In the case the run has its text distributed over several nodes, merge them into one.
     *
     * @param run must be only handled if it contains some special tag or part of it.
     */
    private static void mergeTextNodes(final XWPFRun run) {
        CTR ctr = run.getCTR();
        if (ctr.getTList().size() <= 1) {
            return;
        }
        run.setText(run.text(), 0);

        // the text at position 0 now potentially contains tabs, line feeds and carriage returns which
        //  have to be removed from the run
        while (!ctr.getTabList().isEmpty()) {
            ctr.removeTab(0);
        }

        while (!ctr.getBrList().isEmpty()) {
            ctr.removeBr(0);
        }

        while (!ctr.getCrList().isEmpty()) {
            ctr.removeCr(0);
        }
        // remove the merged text nodes
        while (ctr.getTList().size() > 1) {
            ctr.removeT(1);
        }
    }

    private boolean cannotBeExtendedToValidTag(String partWithPendingTag, String nextPart, int endPos) {
        if (partWithPendingTag.length() - partWithPendingTag.lastIndexOf(START) > MAX_TAG_LENGTH
                || partWithPendingTag.charAt(partWithPendingTag.length() - 1) == START && nextPart.charAt(0) != MARKER) {
            return true;
        }
        int startPos = nextPart.indexOf(START);
        return startPos > -1 && startPos < endPos;
    }
}