function assertEqual(v1,v2) {
   if( v1==v2 ) return;
   var i1=0, i2=0, n1, n2;
   n1 = v1.length;
   n2 = v2.length;
   var ch,ch2;
   while(i1<n1) {
     ch = v1.charAt(i1);
     i1++;
     if( ch==' ' || ch=='\n' || ch=='\r' || ch=='\t' ) continue;
     ch2 = v2.charAt(i2);
     i2=i2+1;
     while( (ch2==' ' || ch2=='\n' || ch2=='\r' || ch2=='\t') && i2<n2 ) {
       ch2 = v2.charAt(i2);
       i2++;       
     }
     if( ch!=ch2 ) {
	   var msg = "Assertion failure, values should be equals should be "+v1+" is actually "+v2 + " at characters "+i1+","+i2+" ch1="+ch+" ch2="+ch2;
	   console.error(msg);
   	   throw new Error(msg);
     }
   }
};

model = { "a" : "eh", "b":2, "c":"sea", "lst":[1,2,3], "lst2":[2,3,4], "person":{"name":"Bill"}, "ampValue":"This & string has two & and two << signs." };
assertEqual("$expected$", testTemplates.toString("$template$",model));
console.log("Passed template $template$ with expected value $expected$\n");
