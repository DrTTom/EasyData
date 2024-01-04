package de.tautenhahn.easydata.docx;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Unit tests for avoiding special tags being distributed over several XML nodes.
 */
class TestDocDefragmenter {

    private DocxDefragmenter testee;
    private List<String> content;

    @BeforeEach
    void createInstance() {
        testee = new DocxDefragmenter();
    }


    /**
     * Document without any fragmented special tags should not be changed.
     */
    @Test
    void noUnnecessaryChange() throws IOException, InvalidFormatException {
        try (InputStream source = TestDocDefragmenter.class.getResourceAsStream("/example.docx");
             OutputStream dest = new FileOutputStream(Paths.get("build", "defrag_example.docx").toFile())) {

            testee.reorganize(source, dest);
            Assertions.assertThat(testee.changes).isEqualTo(0);
        }
    }

    @Test
    @Disabled("TODO: get test data")
    void defragFile() throws IOException, InvalidFormatException {
        try (InputStream source = TestDocDefragmenter.class.getResourceAsStream("/example_fragmented.docx");
             OutputStream dest = new FileOutputStream(Paths.get("build", "defrag_example2.docx").toFile())) {

            testee.reorganize(source, dest);
            Assertions.assertThat(testee.changes).isEqualTo(4);
        }
    }

    /**
     * Text should be moved between runs to make each special tags appear in one single run.
     * Mocking seems strange because of questionable name setText for a method which appends a text node.
     */
    @Test
    void change3Runs() {
        content = new ArrayList<>(List.of("beginning (@", "=", "x.y) further Text"));
        String originalContent= String.join("", content);
        CTR ctr = Mockito.mock(CTR.class);
        Mockito.when(ctr.getTList()).thenReturn(List.of(Mockito.mock(CTText.class)));
        List<XWPFRun> runs = IntStream.range(0, content.size()).mapToObj(i -> {
                    XWPFRun run = Mockito.mock(XWPFRun.class);
            Mockito.when(run.getCTR()).thenReturn(ctr);
            Mockito.when(run.text()).thenAnswer(x -> content.get(i));
            Mockito.doAnswer(x-> content.set(i, (String) x.getArguments()[0])).when(run).setText(ArgumentMatchers.anyString(), ArgumentMatchers.eq(0));
            return run;
                }
        ).toList();

            XWPFParagraph paragraph = Mockito.mock(XWPFParagraph.class);
        Mockito.when(paragraph.getRuns()).thenReturn(runs);

        testee.reorganizeParagraph(paragraph);

        Assertions.assertThat(testee.changes).isEqualTo(2);
        Assertions.assertThat(content.get(0)).contains("(@=x.y)");
        Assertions.assertThat(String.join("", content)).isEqualTo(originalContent);
    }
}
