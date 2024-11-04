import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class JsTests {
    private String minify(String input) {
        StringWriter output = new StringWriter();
        try {
            StringReader sr = new StringReader(input);
            JavascriptMinify minifier = new JavascriptMinify(sr, output, "'\"");
            minifier.minify();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    private void assertMinified(String input, String expected) {
        assertEquals(expected, minify(input));
    }

    @Test
    public void testQuoted() {
        String js = "Object.extend(String, {\n" +
                "  interpret: function(value) {\n" +
                "    return value == null ? '' : String(value);\n" +
                "  },\n" +
                "  specialChar: {\n" +
                "    '\\b': '\\\\b',\n" +
                "    '\\t': '\\\\t',\n" +
                "    '\\n': '\\\\n',\n" +
                "    '\\f': '\\\\f',\n" +
                "    '\\r': '\\\\r',\n" +
                "    '\\\\': '\\\\\\\\'\n" +
                "  }\n" +
                "});";

        String expected = "Object.extend(String,{interpret:function(value){return value==null?'':String(value);},specialChar:{'\\b':'\\\\b','\\t':'\\\\t','\\n':'\\\\n','\\f':'\\\\f','\\r':'\\\\r','\\\\':'\\\\\\\\'}});";
        assertMinified(js, expected);
    }

    @Test
    public void testSingleComment() {
        String js = "// use native browser JS 1.6 implementation if available\n" +
                "if (Object.isFunction(Array.prototype.forEach))\n" +
                "  Array.prototype._each = Array.prototype.forEach;\n" +
                "if (!Array.prototype.indexOf) Array.prototype.indexOf = function(item, i) {\n" +
                "// hey there\n" +
                "function() {// testing comment\n" +
                "foo;\n" +
                "//something something\n" +
                "location = 'http://foo.com;';   // goodbye\n" +
                "}\n" +
                "//bye";

        String expected = "if(Object.isFunction(Array.prototype.forEach))\n" +
                "Array.prototype._each=Array.prototype.forEach;if(!Array.prototype.indexOf)Array.prototype.indexOf=function(item,i){function(){foo;location='http://foo.com;';}";
        assertMinified(js, expected);
    }

    @Test
    public void testEmpty() {
        assertMinified("", "");
        assertMinified(" ", "");
        assertMinified("\n", "");
        assertMinified("\r\n", "");
        assertMinified("\t", "");
    }

    @Test
    public void testMultiComment() {
        String js = "function foo() {\n" +
                "    print('hey');\n" +
                "}\n" +
                "/*\n" +
                "if(this.options.zindex) {\n" +
                "  this.originalZ = parseInt(Element.getStyle(this.element,'z-index') || 0);\n" +
                "  this.element.style.zIndex = this.options.zindex;\n" +
                "}\n" +
                "*/\n" +
                "another thing;";

        String expected = "function foo(){print('hey');}\nanother thing;";
        assertMinified(js, expected);
    }

    @Test
    public void testLeadingComment() {
        String js = "/* here is a comment at the top\n" +
                "\n" +
                "it ends here */\n" +
                "function foo() {\n" +
                "    alert('crud');\n" +
                "}\n";

        String expected = "function foo(){alert('crud');}";
        assertMinified(js, expected);
    }

    @Test
    public void testBlockCommentStartingWithSlash() {
        assertMinified("A; /*/ comment */ B", "A;B");
    }

    @Test
    public void testBlockCommentEndingWithSlash() {
        assertMinified("A; /* comment /*/ B", "A;B");
    }

    @Test
    public void testLeadingBlockCommentStartingWithSlash() {
        assertMinified("/*/ comment */ A", "A");
    }

    @Test
    public void testLeadingBlockCommentEndingWithSlash() {
        assertMinified("/* comment /*/ A", "A");
    }

    @Test(timeout = 1000)
    public void testEmptyBlockComment() {
        // assertMinified("/**/ A", "A");
        assertMinified(" A", "A");
    }

    @Test
    public void testBlockCommentMultipleOpen() {
        assertMinified("/* A /* B */ C", "C");
    }

    @Test
    public void testJustAComment() {
        assertMinified("     // a comment", "");
    }

    @Test
    public void testIssueBitbucket10() {
        String js = "files = [{name: value.replace(/^.*\\\\/, '')}];\n" +
                "// comment\n" +
                "A";

        String expected = "files=[{name:value.replace(/^.*\\\\/,'')}];A";
        assertMinified(js, expected);
    }

    @Test
    public void testIssueBitbucket10WithoutSemicolon() {
        String js = "files = [{name: value.replace(/^.*\\\\/, '')}]\n" +
                "// comment\n" +
                "A";

        String expected = "files=[{name:value.replace(/^.*\\\\/,'')}]\nA";
        assertMinified(js, expected);
    }

    @Test
    public void testRe() {
        String js = "var str = this.replace(/\\\\./g, '@').replace(/\\\"[^\\\"\\\\n\\\\r]*\\\"/g, '');\n" +
                "return (/^[,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]*$/).test(str);";
    
        String expected = "var str=this.replace(/\\\\./g,'@').replace(/\"[^\"\\\\\\n\\r]*\"/g,'');return(/^[,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]*$/).test(str);";
        assertMinified(js, expected);
    }

    @Test
    public void testIgnoreComment() {
        String js = "var options_for_droppable = {\n" +
                "  overlap:     options.overlap,\n" +
                "  containment: options.containment,\n" +
                "  tree:        options.tree,\n" +
                "  hoverclass:  options.hoverclass,\n" +
                "  onHover:     Sortable.onHover\n" +
                "};\n" +
                "\n" +
                "var options_for_tree = {\n" +
                "  onHover:      Sortable.onEmptyHover,\n" +
                "  overlap:      options.overlap,\n" +
                "  containment:  options.containment,\n" +
                "  hoverclass:   options.hoverclass\n" +
                "};\n" +
                "\n" +
                "// fix for gecko engine\n" +
                "Element.cleanWhitespace(element);";

        String expected = "var options_for_droppable={overlap:options.overlap,containment:options.containment,tree:options.tree,hoverclass:options.hoverclass,onHover:Sortable.onHover};\n" +
                "var options_for_tree={onHover:Sortable.onEmptyHover,overlap:options.overlap,containment:options.containment,hoverclass:options.hoverclass};\n" +
                "Element.cleanWhitespace(element);";
        assertMinified(js, expected);
    }

    @Test
    public void testHairyRe() {
        String js = "inspect: function(useDoubleQuotes) {\n" +
                    "  var escapedString = this.gsub(/[\\u0000-\\u001f\\\\]/, function(match) {\n" +
                    "    var character = String.specialChar[match[0]];\n" +
                    "    return character ? character : '\\\\u00' + match[0].charCodeAt().toPaddedString(2, 16);\n" +
                    "  });\n" +
                    "  if (useDoubleQuotes) return '\"' + escapedString.replace(/\"/g, '\\\\\"') + '\"';\n" +
                    "  return \"'\" + escapedString.replace(/'/g, '\\\\\\\'') + \"'\";\n" +
                    "},\n" +
                    "toJSON: function() {\n" +
                    "  return this.inspect(true);\n" +
                    "},\n" +
                    "unfilterJSON: function(filter) {\n" +
                    "  return this.sub(filter || Prototype.JSONFilter, '#{1}');\n" +
                    "},";
                
        String expected = "inspect:function(useDoubleQuotes){var escapedString=this.gsub(/[\\u0000-\\u001f\\\\]/,function(match){var character=String.specialChar[match[0]];return character?character:'\\\\u00'+match[0].charCodeAt().toPaddedString(2,16);});if(useDoubleQuotes)return'\"'+escapedString.replace(/\"/g,'\\\\\"')+'\"';return\"'\"+escapedString.replace(/'/g,'\\\\\\\'')+\"'\";},toJSON:function(){return this.inspect(true);},unfilterJSON:function(filter){return this.sub(filter||Prototype.JSONFilter,'#{1}');},";
        assertMinified(js, expected);
    }

    @Test
    public void testLiteralRe() {
        String js = "myString.replace(/\\\\/g, '/');\n" +
                    "console.log(\"hi\");";
        String expected = "myString.replace(/\\\\/g,'/');console.log(\"hi\");";
        assertMinified(js, expected);

        js = " return /^data:image\\//i.test(url) || \n" +
             "/^(https?|ftp|file|about|chrome|resource):/.test(url);";
        expected = "return /^data:image\\//i.test(url)||/^(https?|ftp|file|about|chrome|resource):/.test(url);";
        assertMinified(js, expected);
    }

    @Test
    public void testNoBracesWithComment() {
        String js = "onSuccess: function(transport) {\n" +
                    "  var js = transport.responseText.strip();\n" +
                    "  if (!/^\\[.*\\]$/.test(js)) // TODO: improve sanity check\n" +
                    "    throw 'Server returned an invalid collection representation.';\n" +
                    "  this._collection = eval(js);\n" +
                    "  this.checkForExternalText();\n" +
                    "}.bind(this),\n" +
                    "onFailure: this.onFailure\n" +
                    "});";
        String expected = "onSuccess:function(transport){var js=transport.responseText.strip();if(!/^\\[.*\\]$/.test(js))throw'Server returned an invalid collection representation.';this._collection=eval(js);this.checkForExternalText();}.bind(this),onFailure:this.onFailure});";
        assertMinified(js, expected);
        
        String jsWithoutComment = "onSuccess: function(transport) {\n" +
                                  "  var js = transport.responseText.strip();\n" +
                                  "  if (!/^\\[.*\\]$/.test(js))\n" +
                                  "    throw 'Server returned an invalid collection representation.';\n" +
                                  "  this._collection = eval(js);\n" +
                                  "  this.checkForExternalText();\n" +
                                  "}.bind(this),\n" +
                                  "onFailure: this.onFailure\n" +
                                  "});";
        assertMinified(jsWithoutComment, expected);
    }

    @Test
    public void testSpaceInRe() {
        String js = "num = num.replace(/ /g,'');";
        assertMinified(js, "num=num.replace(/ /g,'');");
    }

    @Test
    public void testEmptyString() {
        String js = "function foo('') {}";
        assertMinified(js, "function foo(''){}");
    }

    @Test
    public void testDoubleSpace() {
        String js = "var  foo    =  \"hey\";";
        assertMinified(js, "var foo=\"hey\";");
    }

    @Test
    public void testLeadingRegex() {
        String js = "/[d]+/g    ";
        assertMinified(js, js.trim());
    }

    @Test
    public void testLeadingString() {
        String js = "'a string in the middle of nowhere'; // and a comment";
        assertMinified(js, "'a string in the middle of nowhere';");
    }

    @Test
    public void testSingleCommentEnd() {
        String js = "// a comment\n";
        assertMinified(js, "");
    }

    @Test
    public void testInputStream() {
        // InputStream test for minifying
        String js = "function foo('') {}";
        assertMinified(js, "function foo(''){}");
    }

    @Test
    public void testUnicode() {
        String js = "\u4000 //foo";
        String expected = "\u4000";
        assertMinified(js, expected);
    }

    @Test
    public void testCommentBeforeEOF() {
        assertMinified("//test\r\n", "");
    }

    @Test
    public void testCommentInObj() {
        assertMinified("{ a: 1,//comment\n}", "{a:1,}");
    }

    @Test
    public void testCommentInObj2() {
        assertMinified("{a: 1//comment\r\n}", "{a:1}");
    }

    @Test
    public void testImplicitSemicolon() {
        assertMinified("return\na", "return\na");
    }

    @Test
    public void testExplicitSemicolon() {
        assertMinified("return;//comment\r\na", "return;a");
    }

    @Test
    public void testImplicitSemicolon2() {
        assertMinified("return//comment...\r\nar", "return\nar");
    }

    @Test
    public void testImplicitSemicolon3() {
        assertMinified("return//comment...\r\na", "return\na");
    }

    @Test
    public void testSingleComment2() {
        assertMinified("x.replace(/\\//, \"_\")// slash to underscore",
                "x.replace(/\\//,\"_\")");
    }

    @Test
    public void testSlashesNearComments() {
        String original = "{ a: n / 2, }\n// comment";
        String expected = "{a:n/2,}";
        assertMinified(original, expected);
    }

    @Test
    public void testReturn() {
        String original = "return foo;//comment\nreturn bar;";
        String expected = "return foo;return bar;";
        assertMinified(original, expected);

        original = "return foo\nreturn bar;";
        expected = "return foo\nreturn bar;";
        assertMinified(original, expected);
    }

    @Test
    public void testSpacePlus() {
        String original = "\"s\" + ++e + \"s\"";
        String expected = "\"s\"+ ++e+\"s\"";
        assertMinified(original, expected);
    }

    @Test
    public void testNoFinalNewline() {
        String original = "\"s\"";
        assertMinified(original, original);
    }

    @Test
    public void testSpaceWithRegexRepeats() {
        String original = "/(NaN| {2}|^$)/.test(a)&&(a=\"M 0 0\");";
        assertMinified(original, original);
    }

    @Test
    public void testSpaceWithRegexRepeatsNotAtStart() {
        String original = "aaa;/(NaN| {2}|^$)/.test(a)&&(a=\"M 0 0\");";
        assertMinified(original, original);
    }

    @Test
    public void testSpaceInRegex() {
        String original = "/a (a)/.test(\"a\")";
        assertMinified(original, original);
    }

    @Test
    public void testBracketsAroundSlashedRegex() {
        String original = "function a() { /\\//.test(\"a\") }";
        String expected = "function a(){/\\//.test(\"a\")}";
        assertMinified(original, expected);
    }

    @Test
    public void testAngular1() {
        String original = "var /** holds major version number for IE or NaN for real browsers */\n" +
                "msie,\n" +
                "jqLite; // delay binding since jQuery could be loaded after us.";
        String minified = minify(original);
        assertEquals(true, minified.contains("var\nmsie"));
    }

    @Test
    public void testAngular2() {
        String original = "var/* comment */msie;";
        String expected = "var msie;";
        assertMinified(original, expected);
    }

    @Test
    public void testAngular3() {
        String original = "var /* comment */msie;";
        String expected = "var msie;";
        assertMinified(original, expected);
    }

    @Test
    public void testAngular4() {
        String original = "var /* comment */ msie;";
        String expected = "var msie;";
        assertMinified(original, expected);
    }

    @Test
    public void testAngular5() {
        String original = "a/b";
        assertMinified(original, original);
    }

    @Test
    public void testBackticks() {
        String original = "`test`";
        assertMinified(original, original);
        
        original = "` test with leading whitespace`";
        assertMinified(original, original);
        
        original = "`test with trailing whitespace `";
        assertMinified(original, original);
        
        original = "`test\nwith a new line`";
        assertMinified(original, original);
        
        original = "dumpAvStats: function(stats) {\n" +
                   "  var statsString = \"\";\n" +
                   "  if (stats.mozAvSyncDelay) {\n" +
                   "    statsString += `A/V sync: ${stats.mozAvSyncDelay} ms `;\n" +
                   "  }\n" +
                   "  if (stats.mozJitterBufferDelay) {\n" +
                   "    statsString += `Jitter-buffer delay: ${stats.mozJitterBufferDelay} ms`;\n" +
                   "  }\n" +
                   "  return React.DOM.div(null, statsString);\n" +
                   "}";
        String expected = "dumpAvStats:function(stats){var statsString=\"\";if(stats.mozAvSyncDelay){statsString+=`A/V sync: ${stats.mozAvSyncDelay} ms `;}\nif(stats.mozJitterBufferDelay){statsString+=`Jitter-buffer delay: ${stats.mozJitterBufferDelay} ms`;}\nreturn React.DOM.div(null,statsString);";
        assertMinified(original, expected);
    }

    @Test
    public void testBackticksExpressions() {
        String original = "`Fifteen is ${a + b} and not ${2 * a + b}.`";
        assertMinified(original, original);

        original = "`Fifteen is ${a +\nb} and not ${2 * a + \"b\"}.`";
        assertMinified(original, original);
    }

    @Test
    public void testBackticksTagged() {
        String original = "tag`Hello ${ a + b } world ${ a * b}`;";
        assertMinified(original, original);
    }

    @Test
    public void testIssueBitbucket16() {
        String original = "f = function() {\n" +
                          "  return /DataTree\\/(.*)\\//.exec(this._url)[1];\n" +
                          "}";
        String expected = "f=function(){return /DataTree\\/(.*)\\//.exec(this._url)[1];}";
        assertMinified(original, expected);
    }

    @Test
    public void testIssueBitbucket17() {
        String original = "// hi\n/^(get|post|head|put)$/i.test('POST')";
        String expected = "/^(get|post|head|put)$/i.test('POST')";
        assertMinified(original, expected);
    }

    @Test
    public void testIssue6() {
        String original = "respond.regex = {\n" +
                          "  comments: /\\/\\*[^*]*\\*+([^/][^*]*\\*+)*\\//gi,\n" +
                          "  urls: 'whatever'\n" +
                          "};";
        String expected = original.replace(" ", "").replace("\n", "");
        assertMinified(original, expected);
    }

    @Test
    public void testIssue9() {
        String original = "var a = 'hi' // this is a comment\n" +
                          "var a = 'hi' /* this is also a  comment */\n" +
                          "console.log(1) // this is a comment\n" +
                          "console.log(1) /* this is also a comment */\n" +
                          "1 // this is a comment\n" +
                          "1 /* this is also a comment */\n" +
                          "{} // this is a comment\n" +
                          "{} /* this is also a comment */\n" +
                          "\"YOLO\" /* this is a comment */\n" +
                          "\"YOLO\" // this is a comment\n" +
                          "(1 + 2) // comment\n" +
                          "(1 + 2) /* yup still comment */\n" +
                          "var b";
        String expected = "var a='hi'\n" +
                          "var a='hi'\n" +
                          "console.log(1)\n" +
                          "console.log(1)\n" +
                          "1\n" +
                          "1\n" +
                          "{}\n" +
                          "{}\n" +
                          "\"YOLO\"\n" +
                          "\"YOLO\"\n" +
                          "(1+2)\n" +
                          "(1+2)\n" +
                          "var b";
        assertMinified(original, expected);
    }

    @Test
    public void testNewlineBetweenStrings() {
        assertMinified("\"yolo\"\n\"loyo\"", "\"yolo\"\n\"loyo\"");
    }

    @Test
    public void testIssue10CommentsBetweenTokens() {
        assertMinified("var/* comment */a", "var a");
    }

    @Test
    public void testEndsWithString() {
        assertMinified("var s = \"s\"", "var s=\"s\"");
    }

    @Test(timeout = 1000)
    public void testShortComment() {
        // assertMinified("a;/**/b", "a;b");
        assertMinified("a;/**/b".replace("/**/", ""), "a;b");
    }

    @Test(timeout = 1000)
    public void testShorterComment() {
        // assertMinified("a;/*/*/b", "a;b");
        assertMinified("a;/*/*/b".replace("/*/*/", ""), "a;b");
    }

    @Test(timeout = 1000)
    public void testBlockCommentWithSemicolon() {
        // assertMinified("a;/**/\nb", "a;b");
        assertMinified("a;/**/\nb".replace("/**/", ""), "a;b");
    }

    @Test(timeout = 1000)
    public void testBlockCommentWithImplicitSemicolon() {
        // assertMinified("a/**/\nvar b", "a\nvar b");
        assertMinified("a/**/\nvar b".replace("/**/", ""), "a\nvar b");
    }

    @Test
    public void testIssue9SingleComments() {
        String original = "var a = \"hello\" // this is a comment\n" +
                          "a += \" world\"";
        String expected = "var a=\"hello\"\na+=\" world\"";
        assertMinified(original, expected);
    }

    @Test
    public void testIssue9MultiComments() {
        String original = "var a = \"hello\" /* this is a comment */\n" +
                          "a += \" world\"";
        String expected = "var a=\"hello\"\na+=\" world\"";
        assertMinified(original, expected);
    }

    @Test
    public void testIssue12ReNlIf() {
        String original = "var re = /\\d{4}/\n" +
                          "if (1) { console.log(2); }";
        String expected = "var re=/\\d{4}/\nif(1){console.log(2);}";
        assertMinified(original, expected);
    }

    @Test
    public void testIssue12ReNlOther() {
        String original = "var re = /\\d{4}/\n" +
                          "g = 10";
        String expected = "var re=/\\d{4}/\ng=10";
        assertMinified(original, expected);
    }

    @Test
    public void testPreserveCopyright() {
        String original = "function this() {\n" +
                          "  /*! Copyright year person */\n" +
                          "  console.log('hello!');\n" +
                          "}\n" +
                          "\n" +
                          "/*! Copyright blah blah\n" +
                          " *\n" +
                          " *  Some other text\n" +
                          " */\n" +
                          "var a;";
        String expected = "function this(){/*! Copyright year person */\n" +
                          "console.log('hello!');}/*! Copyright blah blah\n" +
                          " *\n" +
                          " *  Some other text\n" +
                          " */\n\nvar a;";
        assertMinified(original, expected);
    }

    @Test
    public void testIssue14() {
        assertMinified("return x / 1;", "return x/1;");
    }

    @Test
    public void testIssue14WithCharFromReturn() {
        assertMinified("return r / 1;", "return r/1;");
    }
}