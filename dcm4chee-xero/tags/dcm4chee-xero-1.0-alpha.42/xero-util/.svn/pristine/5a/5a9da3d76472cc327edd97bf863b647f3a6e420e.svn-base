, "conditions":[ 
	{ "ast": $callast(ast=ast.firstChild)$, "chunks": [$chunk.subtemplate.chunks:astexpr(chunk=it); separator=", "$] } 
$if(chunk.elseIfSubtemplates)$, $chunk.elseIfSubtemplates:{elif|{ 
	"ast": $elif.AST.AST:callast(ast=it);separator=", "$,
	"chunks": [$elif.subtemplate.chunks:astexpr(chunk=it); separator=", "$ ]
} }; separator=", "$ $endif$
	$if(chunk.elseSubtemplate)$, { "ast": true, "chunks":[$chunk.elseSubtemplate.chunks:astexpr(chunk=it); separator=", "$]} $endif$]
