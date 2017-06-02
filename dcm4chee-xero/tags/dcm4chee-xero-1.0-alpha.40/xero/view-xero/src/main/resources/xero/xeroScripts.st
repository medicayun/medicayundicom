$if(firebug)$
  <!-- Already included firebug at top -->
$else$
 <!-- TODO - include this explicitly as part of the overall script -->
 <script type='text/javascript' src='firebug/firebugx.js?v=$version$'> </script>
$endif$

<!-- Combine these into one file, except for the last little script -->
$model/scripts(version=version)$

<script>
/** Define per-user information as required TODO - move this to a child script */
model.userName = "$userName$";  
model.version = "$version$";
</script>
