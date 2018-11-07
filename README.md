# EasyData

Creating some kind of document by inserting data into an appropriate template is a recurring task. There
are many implementations, for instance XSLT for XML, latex for LaTeX, lots of frameworks for HTML.

Just how much functionality do we need to create a useful document? What do we have to assume about the
target language? How complicated will it get?

This is a simple example which targets creation of arbitrary documents using data from a JSON file.
It assumes that there is some template for the documents which contains special expressions to be replaced by data elements.
To be of practical use, the tool should be able to

- stream the document
- choose some characters in the special expressions freely in order not to interfere with the target syntax
- insert data elements at specified positions
- create recurring elements by expanding a FOR-expression
- support conditional elements.

Although this project has been created as example for teaching,
first tests show that creation of LaTeX or DOCX documents already works well enough to be of practical use.

## Separate programming tasks

Following tasks can be given to students or applicants as exercise or test. Do not give the example implementation, obviously.

**Tokenizer (Iterator, Scanner, regular expressions)**

Provide  class 'de.tautenhahn.easydata.TestTokenizer', let the student write a class 'Tokenizer' which passes that test.
An experienced programmer should be able to develop a good idea how to do that in less than an hour or write the class in about two hours.

**Data Access (recursion, generic types, data structures, Stream)**

Discuss the task of accessing parts of a data structure to include into a document. The student shall

- define which data types to support
- write appropriate unit tests in class 'de.tautenhahn.easydata.TestAccessibleData'
- write a class which is able to access collections and String values by attribute path in a complex data structure

**Parsing (reading source code, design pattern, class hierarchy)**

Provide whole project except ResolverFactory and *Tag-Classes. Focus on test 'de.tautenhahn.easydata.TestDataIntoTemplate'.
This is a more complex task which is useful to access the programming and communication skills of an applicant. Discuss
possible classes, do not expect an implemented working solution in a few hours.

**DOCX (ZIP handling, integration, DOCX specification)**

Provide whole project except DocxAdapter. The student shall find out what file inside the DOCX must be handled and hiw to unpack and pack the ZIP.
Discuss security issues with ZIP handling. 

## Usage as application
The application can handle any kind of text based files (for instance HTML, XML, LateX source codes) as well as DOCX documents.
It will not work with documents using lots of internal references (Excel files) or relying on length of elements (ASN.1).

Data must be provided as JSON.

Freely choose beginning, ending and marking character for the special tags to include into the document template. For instance, '(', ')' and '@' will work inside most documents. In that case, all special tags are of form "(@&lt;content&gt;)" which are used in the following explanations.

TODO: define all supported syntax here

TODO: Put comparing operators under test, probably use other operators instead of < and >, call method in AccessibleData to get numeric values right.