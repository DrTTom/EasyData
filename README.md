# EasyData

Creating some kind of document by inserting data into an appropriate template is a recurring task. There
are many implementations, for instance XSLT for XML, luatex for LaTeX, lots of frameworks for HTML.

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
- support definition of macros and values to keep the structure complex templates simple 

Although this project has been created as example for teaching,
first tests show that creation of LaTeX or DOCX documents already works well enough to be of practical use.

## Usage as application
The application can handle any kind of text based files (for instance HTML, XML, LateX source codes) as well as DOCX documents.
It will not work with documents using lots of internal references (Excel files) or relying on length of elements (ASN.1). Call script
specifying data file, template file and output file.

Data must be provided as JSON.

Freely choose beginning, ending and marking character for the special tags to include into the document template. For instance, '(', ')' and '@' will work inside most documents. In that case, all special tags are of form "(@&lt;content&gt;)" which are used in the following explanations. The following special tags are supported:

**(@= &lt;expression&gt;)**

Is replaced by the value specified by expression, where expression may be

- an attribute name, attributes of attributes are written as names separated by dot. Arrays are handled like objects with attribute names "0", "1" and so on.
- expressions containing inner expressions, i.e. if `name` resolves to "Franz", then `element.Franz` can be addressed as `element.${name}` or `element[name]`, alternatively.
- the function `SIZE(&lt;expression&gt;)` is supported as well 

**(@IF &lt;expression1&gt; &lt;operator&gt; &lt;expression2&gt;)&lt;content&gt;(@ELSE)&lt;alternativeContent&gt;(@END)** 

Is replaced by content or alternative content, respectively. Operators may be `==`,`!=`, `&lt;` or `&gt;`, expressions may be as
above or literals surrounded by `"`. The ELSE tag is optional.
 
**(@FOR &lt;name&gt; : &lt;expression&gt; &lt;modifiers&gt; )&lt;content&gt;(@DELIM)&lt;alternativeContent&gt;(@END)**

Is replaced by one copy of content for each value in expression where attribute name is set to that value. Between these contents, alternativeContent 
is inserted. DELIM tag is optional. Expression is as in the "=" tag but must resolve to some array or object and supports the pseudo-attributes `values` and `keys`. Both are optional. For arrays, by default the elements are iterated, for objects by default the keys.
The following modifiers are supported:
- ASCENDING / DESCENDING sort the iterated elements
- UNIQUE removes duplicate elements
- SELECT &lt;expression&gt; replaces each iterated element by some attribute of it.

**(@DEFINE &lt;name&gt; (&lt;params&gt;))&lt;content&gt;(@END)**

Defines a macro of specified name for later use. Params is the comma-separated list of parameter names which should not collide with the data keys. Content may be an arbitrary but well-formed template which can access the global data keys as well as the given parameters.

**(@&lt;name&gt; &lt;params&gt;)**

Is replaced by the resolved content of the macro defined by name. Params is the blank-separated list of expressions which are resolved to the parameter values. That list must have same length as defined in @DEFINE tag.

**(@USE &lt;name&gt; &lt;params&gt;)**

Same as above but name is an expression which is resolved to the macro name.

**(@SET &lt;name&gt;=&lt;expression&gt;)**

Defines an additional data object addressed by name. Make sure name does not collide whith any existing data key.

 
## Usage as library

Read JavaDoc of class `de.tautenhahn.easydata.DataIntoTemplate`.

## Separate programming tasks for teaching

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

Provide whole project except DocxAdapter. The student shall find out what file inside the DOCX must be handled and how to
unpack and pack the ZIP. Discuss security issues with ZIP handling. 
