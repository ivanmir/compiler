# compiler
HCP Java Compiler

Use case for JavaCompiler - to provide the means to compile generated Java classes using custom JAR files as classpaths.

This was designed to run on top of an HCP or Tomcat 8 Profile. Test it under your own for other platforms. 

Please upload the required libraries at WEB-INF/lib folder in order to compile.

<path to your project>\WebContent\WEB-INF\lib\commons-io-2.5.jar
<path to your project>\WebContent\WEB-INF\lib\guava-19.0.jar

The form displays a java class file that uses annotations from Google's Guava library.
So Guava Lib is required no just to build the application, but it is also used to compile the class in runtime.

Instructions:
==================
<ol>
  <li>Place your custom jar file under "WEB-INF/lib" folder</li>
  <li>Add the required code to the input box on the form (index.html) - making use of your custom lib</li>
  <li>Add the relative path to the inputbox below. <- currently it only supports just one JAR file - don't add more than one entry here. The application takes care of checking the path to it and add it to the current classpath in HCP's runtime.</li>
</ol>

Expected Result:
===================
The browser should send you a compiled "class" file
