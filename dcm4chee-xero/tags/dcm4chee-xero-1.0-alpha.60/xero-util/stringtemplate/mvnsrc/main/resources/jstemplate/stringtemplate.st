/**
 * Handles templating as generated client side by a server side JSON version of the parsed template.
 *
 */
 
function StringTemplate(templates) {
	this.templates = templates;
};

this.isDebug = false;
if( this.isDebug ) {
	StringTemplate.prototype.debug = console.debug;
} else {
	StringTemplate.prototype.debug = function(){};
}

StringTemplate.prototype.error = function(msg) {
	console.error(msg);
	throw new Error(msg);
};


StringTemplate.prototype.get = function ST_get(src,val) {
	var ret = src[val];
	if( ret!==undefined ) {
		return ret;
	};
	if( src._parent ) {
		return this.get(src._parent,val);
	};
};

function StringTemplateModel(parent) {
	this._parent = parent;
};

/** The toString method is used to convert information in the model
 * into a string representation of the object, suitable for assignment 
 * via outer or inner HTML values.
 */
StringTemplate.prototype.toString = function ST_toString(templateName, model) {
	if(!templateName) return "StringTemplate Object";
	var template = this.templates[templateName];
	if( template===undefined ) {
	   this.error("Unable to find template",templateName);
	}
	this.debug("Applying "+templateName+" with "+template.chunks.length+" items.");
	var result = this.evalChunks(template.chunks,model);
	return this.convertToString(result);
};	

/** Converts the given result to a string representation of same */
StringTemplate.prototype.convertToString = function ST_convertToString(result) {
	var ret;
	var typ = typeof(result);
	var format;
	if( result===undefined || result===null ) ret = "";
	else if( typ==="string" ) ret = result;
	else if( typ==="object" && result.length!==undefined ) {
		ret = this.listToString(result);
		format = result.format;
	} else {
		ret = ""+result;
		format = result.format;
	}
	if( format==="xml" ) {
		ret = this.formatXml(ret);
	}
	this.debug("Converting %s of type to string result %s",result,ret);
	return ret;
};

/** Formats & and < correctly for XML */
StringTemplate.prototype.formatXml = function ST_formatXml(str) {
	if( !str ) return str;
	return str.replace(/&/g,"&amp;").replace(/</g,"&lt;");
};

StringTemplate.prototype.listToString = function ST_listToString(lst) {	
	this.debug("Converting a list of length %d to a string.", lst.length);
	var i,n=lst.length;
	var ret = "";
	var sep = lst.separator;
	if( typeof(sep)!=="string" ) sep="";
	for(i=0; i<n; i++) {
	    if( i>0 ) ret = ret+sep;
		ret = ret + this.convertToString(lst[i]);
	}
	return ret;
};

/** Applies the given set of chunk data and returns a list of the templated results. */
StringTemplate.prototype.evalChunks = function ST_evalChunks(chunks,model) {
	if( chunks===undefined || chunks===null ) return chunks;
	var ret = new Array();
	var i,n=chunks.length;
	var chunk,val;
	for(i=0; i<n;i++) {
		chunk = chunks[i];
		if( typeof(chunk)==="string" ) {
		  ret.push(chunk);
		  continue;
		};
		this.debug("Evaluating chunk "+i+" of type ASTExpr.");
		val = this.call(chunk.ast[0],model);
		if( chunk.separator && typeof(val)==="object" ) {
			val.separator = chunk.separator;
		}
		if( chunk.format ) {
			if( typeof(val)==="object" ) {
				val.format = chunk.format;
			} else if( typeof(val)==="string" ) {
				val = this.formatXml(val);
			}
		}
		ret.push(val);
	};
	this.debug("Evaluated %d chunks returning %d elements.",n,ret.length);
	return ret;
};

/** Calls the given AST */
StringTemplate.prototype.call=function ST_call(ast,model) {
	if( this[ast.type]===undefined ) this.error("Unknown ast type",ast.type);
	return this[ast.type](ast,model);
};

StringTemplate.prototype.callAsIter = function ST_callAsIter(chunk,model) {
	var ret = this.call(chunk,model);
	if( ret===null || ret===undefined ) return new Array();
	if( typeof(ret)!=="object" ) return [ ret ];
	return ret;
};

/** Apply a given, named template */
StringTemplate.prototype[4] = function ST_ApplyTemplate(chunk,model) {
	this.debug("Trying to apply a template - evaluating args first.");
	var iter = this.callAsIter(chunk.ast[0],model);
	var i,n=iter.length;
	var ret = new Array();
	var args = new Array();
	var target;
	
	var nmodel = new StringTemplateModel(model);
	this.debug("Applying %d iterations of the values.",n);
	for(i=0; i<n; i++) {
		nmodel.i0 = i;
		nmodel.i = i+1;
		nmodel.it = iter[i];
		nmodel[0] = iter[i];
		target = chunk.ast[(i % (chunk.ast.length-1))+1];	
		val = this.call(target,nmodel);
		ret.push(val);		 
	};
	return ret;
};

/** Apply a given named template to multiple iterators */
StringTemplate.prototype[5] = function ST_MultiApply(chunk,model) {
	this.debug("Trying a multi-apply template - evaluating all args up to : first.");
	var iters = new Array();
	var i,j,n;
	var iterCnt = 0;
	for(n=0; chunk.ast[n].type!=22; n++) {
		iters[n] = this.callAsIter(chunk.ast[n],model);
		if( iters[n].length > iterCnt ) iterCnt = iters[n].length;
	};
	var target,val;
	var ret = new Array();
	var nmodel = new StringTemplateModel(model);
	this.debug("Multi-applying %d iterations of %d elements.",iterCnt, n);
	for(i=0; i<iterCnt; i++) {
		nmodel.i0 = i;
		nmodel.i = i+1;
		for(j=0; j<n; j++) {
			val = iters[j];
			if( val!==undefined && val!==null ) val = val[i];
			nmodel[j] = val;
		}
		target = chunk.ast[ i % (chunk.ast.length-n-1) +n+1];
		val = this.call(target,nmodel);
		ret.push(val);
	};
	return ret;
};

/** Include another macro */
StringTemplate.prototype[7] = function ST_Include(chunk,model) 
{
	var nameT = chunk.ast[0];
	var name;
	var argsT = chunk.ast[1];
	if( nameT.type==20 ) {
	  name = nameT.text;
	  if(!name) this.error("No name provided for child include.");
	} else if( nameT.type==33 ) {
		return this.call(nameT,model);
	} else if( nameT.type==9 ) {
	  // The nesting on the AST for this is VERY ugly - it would be much nicer to have
	  // this look like type 20 with arguments after the value of the template to instantiate
	  name = this.call(nameT.ast[0],model);
	  if( !name ) this.error("No name returned executing",nameT.ast[0]);
	  argsT = nameT.ast[1];
	}
	else {
	  throw new Error("Unkonwn include type "+nameT.type);
	}
	var nmodel = model
	var i,n, arg;
	if( argsT && argsT.ast!==undefined ) {
		n = argsT.ast.length;
		this.debug("Including macro %s with %d args.",name,n);
		nmodel = new StringTemplateModel(model);
		for(i=0; i<argsT.ast.length; i++) {
			arg = argsT.ast[i];
			this.call(arg,nmodel);
		}
	} else this.debug("Including macro %s with 0 args.", name);
	return this.toString(name,nmodel);
};

StringTemplate.prototype[8] = function ST_Condition(chunk,model) {
	var conditions = chunk.conditions;
	var i,n=conditions.length;
	var test;
	var condition;
	for(i=0; i<n; i++) {
		condition = conditions[i];
		this.debug("Checking condition "+i +".type="+condition.ast.type);
		test = this.checkif(condition.ast,model);
		if( test ) {
			this.debug("Condition "+i+" was true, evaluating contents:"+condition.chunks);
			return this.evalChunks(condition.chunks,model);
		} 
	}
	return null;
};

/** Convert the child expression to a string and return it */
StringTemplate.prototype[9] = function ST_Value(chunk,model) {
	var val = this.call(chunk.ast[0],model);
	return this.convertToString(val);
};

/** This is a template application, possibly with named arguments */
StringTemplate.prototype[10] = StringTemplate.prototype[7];

/** Checks to see if a given ast definition results in true within the model */
StringTemplate.prototype.checkif = function ST_checkif(test,model) {
	if( test===true ) return true;
	if( this[test.type]===undefined ) {
		this.error("Undefined function for test type",test.type);
	}
	test = this[test.type](test,model);
	if( test===undefined || test===null || test===false || test==="false") return false;
	return true;
};

/** Define an ID object */
StringTemplate.prototype[20] = function ST_ID(chunk,model)
{
	var val= this.get(model,chunk.text);
	this.debug("Looking up ID %s found %s", chunk.text,val);
	return val;
};

/** Define a variable value */
StringTemplate.prototype[21] = function ST_assign(chunk,model)
{
	var name = chunk.ast[0].text;
	var valT = chunk.ast[1];
	// Use the prototype for the SOURCE model information - the current model is the
	// destination.
	var val = this[valT.type](valT,model._parent);
	this.debug("Assigning %s = %s", name,val);
	model[name] = val;
	return "";
};

/** Concat two strings */
StringTemplate.prototype[24] = function ST_concat(chunk,model) {
	var child1 = this.call(chunk.ast[0],model);
	var child2 = this.call(chunk.ast[1],model);
	child1 = this.convertToString(child1);
	child2 = this.convertToString(child2);
	return child1+child2;
};

/** Handle the dot object */
StringTemplate.prototype[25] = function ST_dot(chunk,model) {
	var root = this.call(chunk.ast[0],model);
	var child = chunk.ast[1];
	if( root==undefined ) {
		this.debug("Unable to find parent", chunk.ast[0],"looking for",child); 
		return undefined;
	}
	if( child.type==20 ) {
		var val= this.get(root,child.text);
		return val;
	}
	child = this.call(child,model);
	var val= this.get(root,child);
	return val;
};

/** Handle an anonymous template */
StringTemplate.prototype[33] = function ST_Anonymous(chunk,model) {
	var formal = chunk.formalArguments;
	var nmodel = model;
	var i,n = formal.length;
	if( n>0 ) {
		nmodel = new StringTemplateModel(model);
		for(i=0; i<n; i++) {
			var vname = formal[i];
			if( typeof(vname)=="string" ) {
				this.debug("Assigning "+vname+"="+model[i]);
				nmodel[vname] = model[i];
				nmodel[i] = null;
			}
		}
	}
	return this.evalChunks(chunk.chunks,nmodel);
};

/** Return a string literal */
StringTemplate.prototype[34] = function ST_string(chunk,model) {
	return chunk.text;
};