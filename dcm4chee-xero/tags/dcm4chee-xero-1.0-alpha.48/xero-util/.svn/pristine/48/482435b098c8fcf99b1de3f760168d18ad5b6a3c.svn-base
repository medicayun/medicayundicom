/** This contains a rhino implementation of the console logging facility.  Assume that the rhino.js
 * has already loaded and defined window etc.
 */

if (!("console" in window) || !("firebug" in console)) {
(function()
{
    window.console = 
    {
        log: function()
        {
            logFormatted(arguments, "");
        },
        
        debug: function()
        {
            logFormatted(arguments, "debug");
        },
        
        info: function()
        {
            logFormatted(arguments, "info");
        },
        
        warn: function()
        {
            logFormatted(arguments, "warning");
        },
        
        error: function()
        {
            logFormatted(arguments, "error");
        },
        
        assert: function(truth, message)
        {
            if (!truth)
            {
                var args = [];
                for (var i = 1; i < arguments.length; ++i)
                    args.push(arguments[i]);
                
                logFormatted(args.length ? args : ["Assertion Failure"], "error");
                throw message ? message : "Assertion Failure";
            }
        },
        
        dir: function(object)
        {
            var html = [];
                        
            var pairs = [];
            for (var name in object)
            {
                try
                {
                    pairs.push([name, object[name]]);
                }
                catch (exc)
                {
                }
            }
            
            pairs.sort(function(a, b) { return a[0] < b[0] ? -1 : 1; });
            
            html.push('<table>');
            for (var i = 0; i < pairs.length; ++i)
            {
                var name = pairs[i][0], value = pairs[i][1];
                
                html.push('<tr>', 
                '<td class="propertyNameCell"><span class="propertyName">',
                    escapeHTML(name), '</span></td>', '<td><span class="propertyValue">');
                appendObject(value, html);
                html.push('</span></td></tr>');
            }
            html.push('</table>');
            
            logRow(html, "dir");
        },
        
        dirxml: function(node)
        {
            var html = [];
            
            appendNode(node, html);
            logRow(html, "dirxml");
        },
        
        group: function()
        {
            logRow(arguments, "group", pushGroup);
        },
        
        groupEnd: function()
        {
            logRow(arguments, "", popGroup);
        },
        
        time: function(name)
        {
            timeMap[name] = (new Date()).getTime();
        },
        
        timeEnd: function(name)
        {
            if (name in timeMap)
            {
                var delta = (new Date()).getTime() - timeMap[name];
                logFormatted([name+ ":", delta+"ms"]);
                delete timeMap[name];
            }
        },
        
        count: function()
        {
            this.warn(["count() not supported."]);
        },
        
        trace: function()
        {
            this.warn(["trace() not supported."]);
        },
        
        profile: function()
        {
            this.warn(["profile() not supported."]);
        },
        
        profileEnd: function()
        {
        },
        

    };
 
    // ********************************************************************************************
       
    var messageQueue = [];
    var groupStack = [];
    var timeMap = {};
    
    var clPrefix = ">>> ";
    
    var isFirefox = navigator.userAgent.indexOf("Firefox") != -1;
    var isIE = navigator.userAgent.indexOf("MSIE") != -1;
    var isOpera = navigator.userAgent.indexOf("Opera") != -1;
    var isSafari = navigator.userAgent.indexOf("AppleWebKit") != -1;

    // ********************************************************************************************

    function logRow(message, className, handler)
    {
            writeMessage(message, className, handler);
    }
    
    function flush()
    {
        var queue = messageQueue;
        messageQueue = [];
        
        for (var i = 0; i < queue.length; ++i)
            writeMessage(queue[i][0], queue[i][1], queue[i][2]);
    }

    function writeMessage(message, className, handler)
    {
    	if( typeof(message)==="string" || message.length==undefined) {
    		java.lang.System.out.println(""+message);
    	}
    	else {
    		var i,n=message.length;
    		for(i=0; i<n;i++) {
    			if( message[i]==undefined ) continue;
    			java.lang.System.out.print(""+message[i]);
    		};
	    	java.lang.System.out.println("");
    	}
    }
    
    // ********************************************************************************************

    function logFormatted(objects, className)
    {
        var html = [];

        var format = objects[0];
        var objIndex = 0;

        if (typeof(format) != "string")
        {
            format = "";
            objIndex = -1;
        }

        var parts = parseFormat(format);
        for (var i = 0; i < parts.length; ++i)
        {
            var part = parts[i];
            if (part && typeof(part) == "object" && part.appender)
            {
                var object = objects[++objIndex];
                part.appender(object, html);
            }
            else {
                appendText(part, html);
            }
        }

        for (var i = objIndex+1; i < objects.length; ++i)
        {
            appendText(" ", html);
            
            var object = objects[i];
            if (typeof(object) == "string")
                appendText(object, html);
            else
                appendObject(object, html);
        }
        
        logRow(html, className);
    }

    function parseFormat(format)
    {
        var parts = [];

        var appenderMap = {s: appendText, d: appendInteger, i: appendInteger, f: appendFloat};

		// This is supposed to break the string up into non-format string sections followed by a format key, but it doesn't work.
		var i,n=format.length;
		var l = 0;
        for (i=0; i<n; i++)
        {
        	var ch = format[i];
        	if( ch=='\\\\' ) {
        		i++;
        		continue;
        	}
        	if( ch!='%' ) continue;
        	if( i+1 == n ) break;
        	var type = format[i+1];
            var appender = type in appenderMap ? appenderMap[type] : appendObject;
            if( !appender ) continue;

            parts.push(format.substr(l, i-l) );
            i++;
            l = i+1;
            parts.push({appender: appender});
        }

		if( l<n ) {
		  parts.push(format.substring(l,n-l));
        }

        return parts;
    }

    function escapeHTML(value)
    {
        function replaceChars(ch)
        {
            switch (ch)
            {
                case "<":
                    return "&lt;";
                case ">":
                    return "&gt;";
                case "&":
                    return "&amp;";
                case "'":
                    return "&#39;";
                case '"':
                    return "&quot;";
            }
            return "?";
        };
        return String(value).replace(/[<>&"']/g, replaceChars);
    }

    function objectToString(object)
    {
        try
        {
            return object+"";
        }
        catch (exc)
        {
            return null;
        }
    }

    // ********************************************************************************************

    function appendText(object, html)
    {
        html.push(objectToString(object));
    }

    function appendNull(object, html)
    {
        html.push(objectToString(object));
    }

    function appendString(object, html)
    {
        html.push(objectToString(object));
    }

    function appendInteger(object, html)
    {
        html.push(objectToString(object));
    }

    function appendFloat(object, html)
    {
        html.push(objectToString(object));
    }

    function appendFunction(object, html)
    {
        var reName = /function ?(.*?)\(/;
        var m = reName.exec(objectToString(object));
        var name = m ? m[1] : "function";
        html.push('<span class="objectBox-function">', escapeHTML(name), '()</span>');
    }
    
    function appendObject(object, html)
    {
        try
        {
            if (object === undefined)
                appendNull("undefined", html);
            else if (object === null)
                appendNull("null", html);
            else if (typeof object == "string")
                appendString(object, html);
            else if (typeof object == "number")
                appendInteger(object, html);
            else if (typeof object == "function")
                appendFunction(object, html);
            else if (object.nodeType == 1)
                appendSelector(object, html);
            else if (typeof object == "object")
                appendObjectFormatted(object, html);
            else
                appendText(object, html);
        }
        catch (exc)
        {
        }
    }
        
    function appendObjectFormatted(object, html)
    {
        var text = objectToString(object);
        var reObject = /\[object (.*?)\]/;

        var m = reObject.exec(text);
        html.push('<span class="objectBox-object">', m ? m[1] : text, '</span>')
    }
    
    function appendSelector(object, html)
    {
        html.push('<span class="objectBox-selector">');

        html.push('<span class="selectorTag">', escapeHTML(object.nodeName.toLowerCase()), '</span>');
        if (object.id)
            html.push('<span class="selectorId">#', escapeHTML(object.id), '</span>');
        if (object.className)
            html.push('<span class="selectorClass">.', escapeHTML(object.className), '</span>');

        html.push('</span>');
    }

    function appendNode(node, html)
    {
        if (node.nodeType == 1)
        {
            html.push(
                '<div class="objectBox-element">',
                    '&lt;<span class="nodeTag">', node.nodeName.toLowerCase(), '</span>');

            for (var i = 0; i < node.attributes.length; ++i)
            {
                var attr = node.attributes[i];
                if (!attr.specified)
                    continue;
                
                html.push('&nbsp;<span class="nodeName">', attr.nodeName.toLowerCase(),
                    '</span>=&quot;<span class="nodeValue">', escapeHTML(attr.nodeValue),
                    '</span>&quot;')
            }

            if (node.firstChild)
            {
                html.push('&gt;</div><div class="nodeChildren">');

                for (var child = node.firstChild; child; child = child.nextSibling)
                    appendNode(child, html);
                    
                html.push('</div><div class="objectBox-element">&lt;/<span class="nodeTag">', 
                    node.nodeName.toLowerCase(), '&gt;</span></div>');
            }
            else
                html.push('/&gt;</div>');
        }
        else if (node.nodeType == 3)
        {
            html.push('<div class="nodeText">', escapeHTML(node.nodeValue),
                '</div>');
        }
    }

})();
}
