/**
 * The rectangle specifying the zoomed region of an image.
 */
 
function ZoomRectangle(topLeft, bottomRight) 
{
    if(topLeft && bottomRight)
    {
        this.topLeft = [topLeft[0], topLeft[1]];
        
        this.bottomRight = [bottomRight[0], bottomRight[1]];
    }
    else
    {
        this.topLeft = [0, 0];
        this.bottomRight = [0, 0];
    }
};

/**
 * Gets the width of the rectangle.
 */
ZoomRectangle.prototype.getWidth = function()
{
    return this.bottomRight[0] - this.topLeft[0] + 1;        
};

/**
 * Gets the height of the rectangle.
 */
ZoomRectangle.prototype.getHeight = function()
{
    return this.bottomRight[1] - this.topLeft[1] + 1;        
};

ZoomRectangle.prototype.equals = function(otherRect)
{
    if(!otherRect || !otherRect instanceof ZoomRectangle)
    {
        return false;
    }
    else
    {
        return this.topLeft[0] == otherRect.topLeft[0] &&  
               this.topLeft[1] == otherRect.topLeft[1] &&  
               this.bottomRight[0] == otherRect.bottomRight[0] &&  
               this.bottomRight[1] == otherRect.bottomRight[1];  
    };
};

ZoomRectangle.prototype.toString = function()
{
    return "topLeft: (" + this.topLeft.toString() + ") bottomRight: (" + this.bottomRight.toString() + ")";; 
};