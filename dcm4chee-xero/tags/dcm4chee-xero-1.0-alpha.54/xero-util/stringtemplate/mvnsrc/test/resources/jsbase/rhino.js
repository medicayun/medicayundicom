/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of
 * is contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * This file contains AJAX implementation for Mustang's Rhino based
 * JavaScript engine. Also, we add few hacks to simulate browser-like
 * environment "fool" the existing "AJAX" using scripts.
 * 
 * This allows unit tests and other browser related scripts to run in the rhino JS engine.
 */

var javax = Packages.javax;
var org = Packages.org;

// Few concurrency, asynchronous utilities
var AsyncUtils = new Object();

/**
 * Creates a java.lang.Runnable from a given script
 * function.
 *
 * @param func function that accepts no argument
 * @return a java.lang.Runnable that wraps the function
 */
AsyncUtils.newRunnable = function(func) {
    return new java.lang.Runnable() {
        run: func
    }
};

/**
 * Creates a java.util.concurrent.Callable from a given script
 * function.
 *
 * @param func function that accepts no argument
 * @return a java.util.concurrent.Callable that wraps the function
 */
AsyncUtils.newCallable = function(func) {
    return new java.util.concurrent.Callable() {
          call: func
    }
};


/**
 * Registers the script function so that it will be called exit.
 *
 * @param func function that accepts no argument
 */
AsyncUtils.callAtExit = function (func) {
    java.lang.Runtime.getRuntime().addShutdownHook(
         new java.lang.Thread(AsyncUtils.newRunnable(func)));
};

// The default executor for futures

/* private */ 
AsyncUtils._theExecutor = java.util.concurrent.Executors.newCachedThreadPool(
    new java.util.concurrent.ThreadFactory() {
        newThread: function(r) {
            var th = new java.lang.Thread(r);
            // create daemon threads for futures
            th.setDaemon(true);
            return th;
        }
    });

// clean-up the default executor at exit
AsyncUtils.callAtExit(function() { AsyncUtils._theExecutor.shutdown(); });


/**
 * Executes the function asynchronously with a future task.
 *
 * @param func function that accepts no argument
 * @return a java.util.concurrent.FutureTask
 */
AsyncUtils.submitFuture = function(func) {
    return AsyncUtils._theExecutor.submit(AsyncUtils.newCallable(func));   
};


/**
 * Creates a new Thread that will execute the given
 * function when started.
 *
 * @param func function that accepts no argument
 * @param daemon whether to create a daemon or not [optional]
 *               default value is false
 * @return a java.lang.Thread object
 */
AsyncUtils.newThread = function(func, daemon) {
    if (daemon == undefined) {
        daemon = false;
    }
    var th = new java.lang.Thread(AsyncUtils.newRunnable(func));
    th.setDaemon(daemon);
    return th;
};


// support for locks, condition variables

/**
 * Creates a java.util.concurrent reentrant lock
 *
 * @param fair whether we want a fair lock or not [optional]
 *             default is false
 */
AsyncUtils.newLock = function(fair) {
    if (fair == undefined) {
        fair = false;
    }
    return java.util.concurrent.locks.ReentrantLock(fair);
}

/**
 * Creates a java.util.concurrent reentrant read-write lock
 *
 * @param fair whether we want a fair lock or not [optional]
 *             default is false
 */
AsyncUtils.newRWLock = function(fair) {
    if (fair == undefined) {
        fair = false;
    }
    return java.util.concurrent.locks.ReentrantReadWriteLock(fair);
}



// XML, DOM utilities

// minimal XMLSerializer object -- just serialize XML to String

if (this.XMLSerializer == undefined) {
    this.XMLSerializer = function() {}


    /*
     * Spec: http://www.xulplanet.com/references/objref/XMLSerializer.html
     *
     * Note that this is a partial implementation of XMLSerializer.
     * In particular, no serialize to a stream - only to a string.
     */  

    XMLSerializer.prototype.serializeToString = function(node) {
        if (node == null) {
            return "";
        }

            var source = new javax.xml.transform.dom.DOMSource(node);
            var sw = new java.io.StringWriter();
            var result = new javax.xml.transform.stream.StreamResult(sw);
            var xformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return String(sw.toString());
    };
}

// minimal DOMParser - just parsing from String
if (this.DOMParser == undefined) {
    this.DOMParser = function() {}

    /*
     * Spec: http://www.xulplanet.com/references/objref/DOMParser.html
     *
     * Note that this is a partial implementation of XMLSerializer.
     * We support just parsing from a string source.
     */

    DOMParser.prototype.parseFromString = function(str, contentType) {
        // ignore contentType for now...

            var fac = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            var docBuilder = fac.newDocumentBuilder();
            var r = new java.io.StringReader(str);
            return docBuilder.parse(new org.xml.sax.InputSource(r));
    };
}


// function to create an empty DOM Document
if (this.DOMDocument == undefined) {

    this.DOMDocument = function() {
        var fac = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        return fac.newDocumentBuilder().newDocument();
    }
}

/*
 * The following is not strictly part of AJAX JavaScript API. But,
 * unfortunately, most AJAX scripts assume browser environment and
 * access browser DOM (and other) built-in objects.
 */

/*
 * XMLHttpRequest in checked in "window" object -- which is same 
 * as "global" scope object in browser's embedded JavaScript engine. 
 * This is a hack to simulate the same here.
 */
if (this.window == undefined) {
    this.window = this;
}

/*
 * Hack to simulate "navigator" variable.
 */
if (this.navigator == undefined) {
    this.navigator = { userAgent: "Java" };
}

/*
 * AJAX scripts access "document" object -- sometimes to update current
 * HTML page's DOM but sometimes for generating new DOM Nodes. While we 
 * can't support the former, we can support Document creation. We create 
 * an empty DOM Document object and store it in a global variable.
 */
if (this.document == undefined) {
    this.document = new DOMDocument();
}

/*
 * DOMImplementation singleton object
 */
if (this.DOMImplementation == undefined) {
    this.DOMImplementation = (function() {
            var fac = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance();
            return fac.newDocumentBuilder().getDOMImplementation();
    })();
}

/*
 * 'alert' function - we just println the alert message
 */
if (this.alert == undefined) {
    this.alert = java.lang.System.out.println;
}

/*
 * We don't have the notion of current webpage. User needs to
 * all this to supply the base url of the "current" page.
 * This base URL (if defined) is used by XMLHttpRequest.
 */
function ajaxInit(url) {
   window.location = { pathname: url };
}


var Node = new Object();
Node.ELEMENT_NODE=1;
