package app.services.svg;

import app.dto.RafterCalculationDTO;
import app.util.PartCalculator;

public class CarportSvgTop
{
    private int width;
    private int length;
    private Svg carportTopSvg;
    private Svg carportInnerSvg;
    private final String VIEW_BOX = "0 0 1000 750";
    private final String WIDTH_SIZE = "100%";
    private final String BASE_STYLE = "stroke-width: 1px; stroke:#000000; fill: #ffffff";
    private final String DASHARRAY_STYLE = "stroke:#000000; stroke-dasharray: 5 5";
    private final double RAFTER_WIDTH_CM =  4.5;
    private final double POST_WIDTH_CM = 10.0;
    private final double POST_HEIGHT_CM = 10.0;
    private final double POST_START_POSITION_CM = 100.0;
    private final double POST_VERTICAL_OFFSET_CM = 2.5;
    private final double POST_OFFSET_END_POSITION_CM = 30.00;
    private final double POST_SPACING_CM = 310;
    private final double MAX_SPACING_CM = 55.0;
    private final double POST_EDGE_INSET_CM = 35.00;
    private double yPositionBottom;
    private double yPositionTop;
    private final int INNER_SVG_X_START = 150;
    private final int INNER_SVG_Y_START = 50;
    double arrowXLeftMargin;
    double arrowYEnd;
    double arrowXEnd;
    double arrowYBottomMargin;
    double arrowYTopMargin;


public CarportSvgTop(int width, int length)
{
   this.width = width;
   this.length = length;
   this.carportTopSvg = new Svg(0, 0, WIDTH_SIZE, VIEW_BOX);
   this.carportInnerSvg = new Svg(INNER_SVG_X_START, INNER_SVG_Y_START, this.length, this.width, getInnerViewBox(width, length));
   this.yPositionBottom =  width - POST_EDGE_INSET_CM - 2.5;
   this.yPositionTop = POST_EDGE_INSET_CM + RAFTER_WIDTH_CM;

   this.arrowXLeftMargin = INNER_SVG_X_START / 2;
   this.arrowYEnd = width + INNER_SVG_Y_START;
   this.arrowXEnd = length + INNER_SVG_X_START;
   this.arrowYBottomMargin = width + (INNER_SVG_Y_START * 1.5);
   this.arrowYTopMargin = INNER_SVG_Y_START * 0.75;

   carportTopSvg.addArrowDefs();
   addArrows();
   addArrowText();
   addFrame();
   addBeams();
   addRafters();
   addPost();
   addMetalStrap();
   carportTopSvg.addSvg(carportInnerSvg);
}

private void addFrame()
{
    carportInnerSvg.addRectangle(0,0,width,length, BASE_STYLE);
}

   private void addBeams()
   {
    carportInnerSvg.addRectangle(0, POST_EDGE_INSET_CM, RAFTER_WIDTH_CM, length, BASE_STYLE);
    carportInnerSvg.addRectangle(0,width - POST_EDGE_INSET_CM, RAFTER_WIDTH_CM, length, BASE_STYLE);
   }


    private void addPost()
    {
        int numberOfPostsPerRow = PartCalculator.calculateNumberOfPostsWithOutShed(length);

        double lastPostPosition = length - POST_OFFSET_END_POSITION_CM;

        if(numberOfPostsPerRow == 2)
        {
            addPostPair(POST_START_POSITION_CM);
            addPostPair(lastPostPosition);
        }
        else
        {
            double middlePostPosition = POST_START_POSITION_CM + POST_SPACING_CM;

            addPostPair(POST_START_POSITION_CM);
            addPostPair(middlePostPosition);
            addPostPair(lastPostPosition);
        }

    }

    private void addPostPair(double x)
    {
        carportInnerSvg.addRectangle(x, POST_EDGE_INSET_CM - POST_VERTICAL_OFFSET_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(x, yPositionBottom, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
    }

    private void addRafters()
    {
        RafterCalculationDTO rafterCalcDTO = PartCalculator.calculateRafters(length, RAFTER_WIDTH_CM);

        int numberOfRafters = rafterCalcDTO.numberOfRafters();
        double spacing = rafterCalcDTO.spacing();
        double currentXPos = 0;

        for(int i = 0; i < numberOfRafters -1; i++)
        {
            carportInnerSvg.addRectangle(currentXPos, 0, width, RAFTER_WIDTH_CM, BASE_STYLE);
            currentXPos += spacing;
        }
        carportInnerSvg.addRectangle(length - RAFTER_WIDTH_CM, 0, width, RAFTER_WIDTH_CM, BASE_STYLE);
    }

/*
private void addPost()
{
    double postSpacing = 310;

    for(double x = POST_START_POSITION_CM; x < length; x += postSpacing)
    {
        carportInnerSvg.addRectangle(x, POST_EDGE_INSET_CM-2.5, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(x, yPositionBottom, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
    }
}

 */

    /*
private void addRafters()
{
    int numberOfSpaces = (int) Math.round(length / MAX_SPACING_CM);
    double spacing = (double) length / numberOfSpaces;

    for(double x = 0; x < length; x+=spacing)
    {
        carportInnerSvg.addRectangle(x, 0, width, RAFTER_WIDTH_CM, BASE_STYLE);
    }

}
 */

private void addMetalStrap()
{
    double metalStrapEnd = Math.round(length * 0.712);
    double xPosStart = MAX_SPACING_CM + RAFTER_WIDTH_CM;
    double ySpacing = 5;

    carportInnerSvg.addLine(xPosStart, yPositionTop, metalStrapEnd, yPositionBottom, DASHARRAY_STYLE);
    carportInnerSvg.addLine(xPosStart, yPositionTop + ySpacing, metalStrapEnd, yPositionBottom + ySpacing, DASHARRAY_STYLE);

    carportInnerSvg.addLine(xPosStart, yPositionBottom, metalStrapEnd, yPositionTop, DASHARRAY_STYLE);
    carportInnerSvg.addLine(xPosStart, yPositionBottom + ySpacing, metalStrapEnd, yPositionTop + ySpacing, DASHARRAY_STYLE);
}

private void addArrows()
{
    //TODO FIX HARDCODING

    // Top arrow
    int numberOfSpaces = (int) Math.round(length / MAX_SPACING_CM);
    double spacing = (double) length / numberOfSpaces;

    for(int i = 0; i < numberOfSpaces; i++) {
        double x1 = INNER_SVG_X_START + (i * spacing);
        double x2 = INNER_SVG_X_START + ((i + 1) * spacing);
        double y = arrowYTopMargin;

        carportTopSvg.addLineWithArrows(x1, y, x2, y);

        double midX = (x1 + x2) / 2.0;
        double spacing_meters = spacing / 100.0;
        carportTopSvg.addText(midX, y - 15, 0, String.format("%.2f", spacing_meters));
    }
    // Left arrow
    carportTopSvg.addLineWithArrows(arrowXLeftMargin, INNER_SVG_Y_START, arrowXLeftMargin, arrowYEnd);

    //Left inner arrow
    carportTopSvg.addLineWithArrows(INNER_SVG_X_START * 0.75, INNER_SVG_Y_START + POST_EDGE_INSET_CM, INNER_SVG_X_START * 0.75, arrowYEnd - POST_EDGE_INSET_CM);

    // Bottom arrow
    carportTopSvg.addLineWithArrows(INNER_SVG_X_START, arrowYBottomMargin, arrowXEnd, arrowYBottomMargin);
}

private void addArrowText()
{
    carportTopSvg.addText(arrowXLeftMargin - 10, arrowYEnd / 2, -90, String.valueOf(width));
    carportTopSvg.addText(arrowXEnd / 2, arrowYBottomMargin + 15, 0, String.valueOf(length));

    //TODO FIX hardcoding Inner text
    int innerArrowLength = (int)(width - (2 * POST_EDGE_INSET_CM));
    carportTopSvg.addText(INNER_SVG_X_START * 0.75 - 10, arrowYEnd / 2, -90, String.valueOf(innerArrowLength));
}

private String getInnerViewBox(int width, int length)
{
    String widthString = String.valueOf(width);
    String lengthString = String.valueOf(length);

    return "0 0 " + lengthString + " " + widthString;
}

    @Override
    public String toString() {
        return carportTopSvg.toString();
    }
}