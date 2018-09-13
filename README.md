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

Although this project has been created as example for teaching,
first tests show that creation of LaTeX documents already works well enough to be of practical use.