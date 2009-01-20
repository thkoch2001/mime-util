This is being released by Medsea Business Solutions S.L. under the Apache Licence V2.

For Usage instructions see the end of this file and the unit tests provided with this utility.



MimeUtil Mime Type Detection utility is a very light weight utility that depends only on apache commons-logging.
It requires at least Java 1.4 as it uses the java.nio library

This document explains how mime types are detected and reported by this utility.

A mime or "Multipurpose Internet Mail Extension" type is an Internet standard that is important outside of just e-mail use.
Mime is used extensively in other communications protocols such as HTTP for web communications.
IANA "Internet Assigned Numbers Authority" is responsible for the standardisation and publication of mime types. Basically any
resource on any computer that can be located via a URI can be assigned a mime type. So for instance, JPEG images have a mime type
of image/jpg. Some resources can have multiple mime types associated with them such as files with an XML extension have the mime types
text/xml and application/xml and even specialised versions of xml such as image/svg+xml for SVG image files. The list of mime types is quite
extensive but if you should require a new mime type you can always specify your own. Obviously if you transmit this information specifying
your own mime types, others around the world would not know what the information should represent, but you would be able to create clients
and applications that handle resources with these specific mime type in-house no problem at all.

Anyway, mime types have been around on the Internet nearly as long as the Internet itself and because of the length of time many
mime types have been standardised such that applications can be written to understand and handle information transmitted over the
web in specific standardised ways. For instance HTML pages are transmitted with a mime type of text/html and browsers know what
to do with this type of information.

Mostly mime types for files can be assumed based on their file extension such as files ending in .jar are assigned the mime type
application/java-archive but some files do not have extensions such as Make files and this association is then not possible.
In these cases an alternative to mime type association by file extension is needed. Luckily within the Unix world, file type
detection using magic numbers has been around for quite some time, even before the Internet. This technique peaks inside files to
known offsets and compares values. It does this based on a set of rules contained in a file called 'magic' located on the Unix file system.
The Unix 'file' command uses this file, or a compiled version of it, to determine information about the file. It recursively passes
the file through the rules defined in the 'magic' file until a match is found, or not. However, the 'magic' file itself does not contain
mime type information so Unix again comes to the rescue with an alternative file called 'magic.mime' which has the exact same rule set as the
'magic' file but can guess the mime types of the files based on the magic rule set. The down side of this detection method is that
it's much slower than association by file extension. Mime type detection is not guaranteed to be accurate. It is a best guess
approach where speed should be of greater consideration than accuracy. This mime utility uses the two techniques described
above.

This utility can retrieve a mime type using a files extension (if it has one) by checking this against the internally built list of registered
extensions within the MimeUtil class.

The list is build in the following order:

1. Load the properties file from the mime utility jar named eu.medsea.mime.mime-types.properties.
2. Locate and load a file named .mime-types.properties from the users home directory if one exists.
3. Locate and load a file named mime-types.properties from the classpath if one exists
4. locate and load a file named by the JVM property mime-mappings i.e. -Dmime-mappings=../my-mime-types.properties

In steps 2 to 4 above, if a mapping exists for an extension that was loaded in a previous step then this new mapping takes precedence.
If in steps 2 to 4 new mappings are defined then these are added to the internally supported list of mappings. Using file extension mappings it
is possible to have more than one mime type associated with a file, each one separated by a comma as in the following example:

hlp=application/hlp,application/x-helpfile,application/x-winhelp,application/winhlp

This entry (taken from the internal mime-types.properties file) associates the extension .hlp with the comma separated list of mime types. So
unless you over ride this mapping this is the list that will be returned by the utility if you use the the file extension mapping methods.

NOTE 1> The mime types are NOT additive, so if you add a different association for .hlp files in one of the files used during initialisation then your
new mappings will override the previously defined mapping.

NOTE 2> ALL the extensions should be added in lower case. The reason for this is to cater for case insensitive OS's such as windows. For instance, windows
does not distinguish the difference between .BAT .Bat .bat .bAt & .baT as a valid file extension for the same file type. This is another reason that mime
mapping using a files extension is not an exact science. Internally the mime utility will try to convert the extension to lower case.

Fortunately, we have compiled a relatively large list of mappings into a java properties file from information gleaned from many sites on the Internet.
This file resides in the eu.medsea.util.mime-types.properties file and is not guaranteed to be correct or contain all the known mappings for a file
extension type. This is not a complete or exhaustive list as that would have proven too difficult to compile for this project.
So instead we give you the opportunity to extend and override these mappings for yourself as defined above.

Obviously, to use this method you don't actually need a file object, you just need a file name with an extension. Also, if you have given or renamed a
file using a different extension than the one that it would normally be associated with then this mapping will return the wrong mime-type and
if the file has no extension at all, such as Make, then it's not going to be possible to determine a mime type using this technique
(see the next section for a better guessing algorithm under these conditions).


The second method of detection provided by the utility is to use Unix 'magic.mime' file processing. If you are not using a Unix system or your system
does not have a 'magic.mime' file, don’t worry, we have included a copy within the project and if we can't locate the files on your system we will use
this internal version instead. This mechanism is much more processor intensive than the first case of file extension association so it is
much better for you and your systems performance if you can define you previously unknown file extensions in your extended mime-types.properties files
some how. However, this is not always possible. In one particular case we had a CMS (Content Management System) that stored all of its images within a
directory structure and renamed them all with a .img extension no matter what the original extension was. So it would not be possible to say that .img
files were really .jpg files as even though this was correct in the majority of cases, things fell apart when it was not a JPEP image but rather a GIF
image or even worse an SVG (xml format) image. In these cases and in the other case where a file has no extension at all we use the Unix magic number mechanism.

Magic numbers are information contained in a file that identifies this file as being 'of a particular' type. Again this is not always
a precise science, just a best guess according to rules configured in the 'magic.mime' file. However, Almost all file types have some
kind of header or marker that identifies them as containing a specific type of content. These can be values at a well known offset
within a file or a relative offset defining another offset of where to find information within the file that can be used to identify it.

A full description of magic numbers is beyond the scope of this document so if you want to know more about them then take google for
a test drive. The mime utility takes the 'magic.mime' files, compiles a recursive decent tree from the contents and then stores this internally
for content comparison (so it's pretty fast anyway). Each file passed to this detection process is tested against each condition in turn until
either a match is found or the end of the conditions are reached. If a condition matches and it has sub-conditions these are also then
tested and only if all sub-conditions trees match is a test assumed to be passed. In the case of a match the mime type is returned.
In the case of the 'magic.mime' file only a single mime type seems to ever be returned (don't know if its possible to return more than one).
If no matches are found then the mime utility returns "application/octet-stream". If you want to distinguish the difference between a "no match"
from content that actually does match and returns the mime type "application/octet-stream" from the magic mime filter processing steps then use the
setUnknownMimeType(String mimeType) method to change the returned mime type. For instance you can set this to a custom mime type such as
application/x-unknown-mime-type and your application can then take specific actions on a real "no match" verses a match that could return application/octet-stream.
The other mime type that we use extensively is "application/directory" this is not a standard mime type but is apparently used and agreed by many
people on the Internet and may yet be standardised in the future, so we decided this was OK to use.

We have also provided an extension capability to the 'magic.mime' file along with the mime-types.properties file. You just create your
own magic.mime files using the format defined by the Unix 'magic.mime' file and place these in locations the utility can find on your classpath,
home directory and as a JVM property. The exact order of location and loading is as follows:

1. From a JVM system property magic-mime i.e -Dmagic-mime=../my/magic/mime/rules
2. From any file named magic.mime that can be found on the classpath
3. From a file named .magic.mime in the users home directory
4. From the normal Unix locations /usr/share/file/magic.mime and /etc/magic.mime (in that order)
5. From the internal magic.mime file eu.medsea.util.magic.mime if, and only if, no files are located in step 4

So as you can see, plenty of places you can extend the default rules.

The order is defined as most specific to least specific, so if you want to create your own rule for mapping SVG files just put the rule in one
of the files nearer the beginning of the load order (this is the opposite from the mime-types.properties load order).

A description of the magic.mime format is beyond the scope of this document so check it out on the net by looking up the Unix 'file' command.
This load order means that you can define your own rules as well as fix any broken rules you may find and add any missing rules where you can
determine a files type from well known information contained at various locations within the file. We have provided one extension to the 'magic.mime'
processing rules that is not supported by the Unix 'file' command, and you can use this extension mechanism in your own 'magic.mime' file rules,
see the next section for a description on how to use this extension mechanism.
DO NOT modify the internal copy of 'magic.mime' OR the original 'magic.mime' files located on your Unix system.

We extended the string type rule which allows you to match strings in a file where you do not know the actual offset of the string containing magic
file information it goes something like “what I am looking for will be ‘somewhere’ within the next n characters” from this location.
This is an important improvement to the string matching rules especially for text based documents such as HTML and XML formats.
The reasoning for this was that the rules for matching SVG images defined in the original 'magic.mime' file hardly ever worked, this is
because of the fixed offset definitions within the magic rule format. As XML documents generally have an XML declaration that can
contain various optional attributes the length of this header often cannot be determined, therefore we cannot know that the DOCTYPE
declaration for an SVG xml file starts at “this” location, all we can say is that, if this is an SVG xml file then it will have an SVG DOCTYPE
somewhere near the beginning of the file and probably within the first 1024 characters. So we test for the xml declaration and then we test for
the DOCTYPE within a specified number of characters and if found then we match this rule. This extension can be used to better identify ALL of the
XML type mime mappings in the current 'magic.mime' file. Remember though, as we stated earlier mime type matching using any of the mechanisms supported
is not an exact science and should always be viewed as a 'best guess' and not as a 'definite match'.


An example of overriding the PNG and SVG rules can be found in our internal 'magic.mime' file located in the test_files directory (this file is NOT used
when locating rules and is used for testing purposes only).
This PNG rule overrides the original PNG rule defined in the 'magic.mime' file we took from the Internet, and the SVG rule overrides the SVG detection also
defined in the original 'magic.mime' file

#PNG Image Format
0		string		\211PNG\r\n\032\n		image/png

#SVG Image Format
#	We know its an XML file so it should start with an XML declaration.
0	string	\<?xml\ version=	text/xml
#	As the XML declaration in an XML file can be short or extended we cannot know
#	exactly where the declaration ends i.e. how long it is,
#	also it could be terminated by a new line(s) or a space(s).
#	So the next line states that somewhere after the 15th character position we should find the DOCTYPE declaration.
#	This DOCTYPE declaration should be within 1024 characters from the 15th character
>15	string>1024<	\<!DOCTYPE\ svg\ PUBLIC\ "-//W3C//DTD\ SVG 	image/svg+xml

As you can see the extension is defined using the syntax string>bufsize<. It can only be used on a string type and basically
means match this within bufsize character from the position defined at the beginning of the line. This rule is much more verbose than
required as we really only need to check for the presence of SVG. As we said earlier, this is a test case file and not used by the utility
under normal circumstances.

The test mime-types.properties and magic.mime files we use can be located in the test_files directory of this distribution.

Because it's possible to associate more than one mime type with a file, especially when using file extension mapping, we provide utility methods that can
take a comma separated list of wanted mime types and return a single mime type that matches the best (if we can). This means that you can pass in say,
the browser Accept header of a request and we will return the mime type that gives you the best match according to the following rules:

For the following examples we will assume that the Accept header from your browser is:
Accept: text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, video/x-mng, image/png, image/jpeg, image/gif;q=0.2, text/css, text/*
As well as these mime types we also append a */*;q=0.1 entry unless */* is actually present. So the actual list of mime types we assume acceptable to the client are:
text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, video/x-mng, image/png, image/jpeg, image/gif;q=0.2, text/css, text/*, */*;q=0.1

1. If there is only one mime type associated with a file then this is what we return even if the list you passed in does not include this mime type as it will match */*.
	For example: The returned mime type for the file extension .tar is application/x-tar as it only matches the */*;q=0.1

2. Next we put the passed in entries into an ordered map. This is keyed on the major mime types such as text and application, in the order they first appear. This gives us:
	text/xml
		/html;q=0.9
		/plain;q=0.8
		/css
		/*
	application/xml
	           /xhtml=xml
	video/x-mng
	image/png
	     /jpeg
	     /gif;q=0.2
	*/*

3. If more than one mime type is associated with a file then we remove all entries from the map that are not supported.
	For example: The returned mime types for the file extension .xhtml are application/xhtml+xml and text/html (in this order). This leaves us with:
		text/html;q=0.9
		application/xhtml+xml

	As the application/xhtml+xml entry has a higher QoS (Quality of Service) indicator than text/html;q=0.9 this is the mime type that will be returned.
	In this case it was also the preferred i.e. the first in the list of supported mime types. If the precedence had been the other way around in the accept
	header we would still have returned application/xhtml+xml as this has the higher QoS.

4. If multiple mime types match with the same precedence then we return the first match i.e.
	If the Accept header had both text/html and application/xhtml+xml at the same QoS it would have automatically returned text/html as it was the first
	in the list defined in the Accept header. You need to take cosideration of this rule.

5. If there is a general type (wild card) in the Accept header with no QoS defined such as text/* we give a default QoS. For a wild card in the major mime component
	such as */* we give this a QoS of 0.01 unless a QoS is specifically defined such as */*;q=0.5 in which case this will match ALL returned types and give them a QoS
	of 0.5. If there are more specific types i.e. text/xml;q=0.9 then text/xml will have another entry with a higher QoS of 0.9. The default assigned QoS for a sub type
	is 0.02 i.e. higher than the default for a major component wild card.

The following list shows the order of precedence for a single major mime type:
	a) text/html;level=1 - text/html - text/html;q=1.0 (ALL equivalent)
    b) text/html;q=0.8
    c) text/* (given a default QoS of 0.02)
    d) */* (given a default QoS of 0.01)

    Entries wih the same precendece are kept in the order they appear in the Accept header, and returned in that order. So an Accept header of:
    Accept: text/html, text/plain, text/xml;q=1.0, text/url has 4 entries all of which carry the same precedence. So unless a specific match occures,
    i.e. say for text/xml then the first mime type in this list will be returned.


There is a fairly comprehensive set of unit tests available for the project so if you need a more indepth explanation of how the utility works
check out the source code and unit tests.

TO CONTRIBUTE:::

Please register any bugs you find on sourceforge and we will do our best to address them ASAP.

It would also be very helpful for you to email in modifications to mime types currently listed in the mime-types.properties file.
It would also be great if people would send in any modifications or new rules to the mime rules in the magic.mime types.
These can be emailed to smcardle@smcardle.com. We will list the additions/changes to mime-types.properties and magic.mime files on sourceforge as a
seperated downloads for the project. This will allow all users to be able to locate these rules easily. We expect that we will add any changes to
the mime-type.properties with each new release. However, for the magic.mime rules file we will just keep this up to date on sourceforge as we do not want
to adversely affect installations that rely on the Unix system native magic.mime files for correct use i.e. if we rolled these changes into the internal file,
they would not be seen on systems supporting magic.mime such as ALL versions of Unix and Linux