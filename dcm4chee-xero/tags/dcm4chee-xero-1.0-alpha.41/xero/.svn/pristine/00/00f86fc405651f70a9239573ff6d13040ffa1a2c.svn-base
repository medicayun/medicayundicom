/**
 * Handles a Sorting of columns in the StudyData results.
 */
function SortPatient(src) {
    this.layout = src;
};

/**
 * Gets the given item as a lower-case string.  If the given item
 * is null or undefined, then empty string is returned.
 */
SortPatient.getItemString = function(item)
{
    if(item)
    {
        return String(item).toLowerCase();
    }
    else
    {
        return "";
    }
};

/**
 * Gets the given item as a number.  If the given item
 * is null or undefined, then zero is returned.
 */
SortPatient.getItemNum = function(item)
{
    if(item)
    {
        return parseFloat(item);
    }
    else
    {
        return 0;
    }
};

/**
 * Sort study result column
 */
SortPatient.prototype.sortColumn = function (col)   {
    var data = this.layout.search.patient;
    this.img = [];
    
    /**
     * Ascending numeric compare function for top level elements of the study result
     */
    var compareNumAsc = function (item1, item2)  {
        var item1Num = SortPatient.getItemNum(item1[col.sort]);
        var item2Num = SortPatient.getItemNum(item2[col.sort]);
        return  item1Num - item2Num;
    }
      
    /**
     * Descending numeric compare function for top level elements of the study result
     */      
    var compareNumDesc = function (item1, item2)  {
        var item1Num = SortPatient.getItemNum(item1[col.sort]);
        var item2Num = SortPatient.getItemNum(item2[col.sort]);
        return  item2Num - item1Num;
    }
      
    /**
     * Ascending lexicographic compare function for top level elements of the study result
     */
    var compareLexAsc = function (item1, item2)  {
        var item1Str = SortPatient.getItemString(item1[col.sort]);
        var item2Str = SortPatient.getItemString(item2[col.sort]);
        return item1Str.localeCompare(item2Str);
    }
      
    /**
     * Descending lexicographic compare function for top level elements of the study result
     */      
    var compareLexDesc = function (item1, item2)  {
        var item1Str = SortPatient.getItemString(item1[col.sort]);
        var item2Str = SortPatient.getItemString(item2[col.sort]);
        return item2Str.localeCompare(item1Str);
    }
      
    /**
     * Ascending numeric compare function for top level elements of the study result. 
     * comparison based on the first child.
     */      
    var compareNumAscFirstChild = function (item1, item2)  {
        var item1Num = SortPatient.getItemNum(item1.study[0][col.sort]);
        var item2Num = SortPatient.getItemNum(item2.study[0][col.sort]);
        return  item1Num - item2Num;
    }
      
    /**
     * Descending numeric compare function for top level elements of the study result. 
     * comparison based on the first child.
     */            
    var compareNumDescFirstChild = function (item1, item2)  {
        var item1Num = SortPatient.getItemNum(item1.study[0][col.sort]);
        var item2Num = SortPatient.getItemNum(item2.study[0][col.sort]);
        return  item2Num - item1Num;
    }  
    
    /**
     * Ascending lexicographic compare function for top level elements of the study result. 
     * comparison based on the first child.
     */      
    var compareLexAscFirstChild = function (item1, item2)  {
        var item1Str = SortPatient.getItemString(item1.study[0][col.sort]);
        var item2Str = SortPatient.getItemString(item2.study[0][col.sort]);
        return item1Str.localeCompare(item2Str);
    }
      
    /**
     * Descending lexicographic compare function for top level elements of the study result. 
     * comparison based on the first child.
     */            
    var compareLexDescFirstChild = function (item1, item2)  {
        var item1Str = SortPatient.getItemString(item1.study[0][col.sort]);
        var item2Str = SortPatient.getItemString(item2.study[0][col.sort]);
        return item2Str.localeCompare(item1Str);
    }  
    
    /**
     * Sort the columns that has the level one study elements   
     */    
    var sortLevelOne = function (val, or) {
        if (or == "asc")   
        {
            if(col.sortType == "numeric")
            {
                val.sort(compareNumAsc);
            }
            else
            {
                val.sort(compareLexAsc);
            }
        } 
        else  
        {
            if(col.sortType == "numeric")
            {
                val.sort(compareNumDesc);
            }
            else
            {
                val.sort(compareLexDesc);
            }
        }
    };  
    
    /**
     * Sort the columns that has the level two study elements   
     */    
    var sortLevelTwo = function (val, or) {
        var compFunc;
        var compFirstChildFunc;
        
        if (or == "asc")    
        {
            compFunc = compareLexAsc;
            compFirstChildFunc = compareLexAscFirstChild;
            if(col.sortType == "numeric")
            {
                compFunc = compareNumAsc;
                compFirstChildFunc = compareNumAscFirstChild
            }
        } 
        else 
        {
            compFunc = compareLexDesc;
            compFirstChildFunc = compareLexDescFirstChild;
            if(col.sortType == "numeric")
            {
                compFunc = compareNumDesc;
                compFirstChildFunc = compareNumDescFirstChild
            }
        };
        
        for(i in val)
        {
            val[i].study.sort(compFunc);
            for (j in val[i].study) {
                val[i].study[j].xmlFirst=false;
            }
            val[i].study[0].xmlFirst=true;
        }
        val.sort(compFirstChildFunc);
    };    
  
    if (this[col.sort] == undefined || this[col.sort] == "desc")  {
        this[col.sort]="asc";
        this.img[col.sort] = "icon/up_arrow_32.png";
    } else  {
        this[col.sort]="desc";
        this.img[col.sort] = "icon/down_arrow_32.png";
    }
    
    // Check if it's Patient Name or Patient ID
    if (col.level == "1")  {
        sortLevelOne(data, this[col.sort]);
        console.info("%s IMAGE %s",col.sort, this.img[col.sort]);
    }
  
    // For Study columns
    if (col.level == "2")    {
        sortLevelTwo(data, this[col.sort]);
        console.info("%s IMAGE %s",col.sort, this.img[col.sort]);
    }    
};


