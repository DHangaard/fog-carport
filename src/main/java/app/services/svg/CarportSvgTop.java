package app.services.svg;

import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.entities.Shed;
import app.util.PartCalculator;
import app.util.PostPlacementCalculatorUtil;

import java.util.List;

public class CarportSvgTop
{
    private Carport carport;
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
    double arrowLeftXStart;
    double arrowInnerLeftXStart;
    double arrowBottomY;
    double arrowBottomXEnd;
    double arrowYBottomMargin;
    double arrowYTopMargin;


    public CarportSvgTop(Carport carport)
    {
        this.carport = carport;
        this.carportTopSvg = new Svg(0, 0, WIDTH_SIZE, VIEW_BOX);
        this.carportInnerSvg = new Svg(INNER_SVG_X_START, INNER_SVG_Y_START, carport.getLength(), carport.getWidth(), getInnerViewBox(carport.getWidth(), carport.getLength()));
        this.yPositionBottom =  carport.getWidth() - POST_EDGE_INSET_CM - 2.5;
        this.yPositionTop = POST_EDGE_INSET_CM + RAFTER_WIDTH_CM;

        this.arrowLeftXStart = INNER_SVG_X_START / 2;
        this.arrowInnerLeftXStart = INNER_SVG_X_START * 0.75;
        this.arrowBottomY = carport.getWidth() + INNER_SVG_Y_START;
        this.arrowBottomXEnd = carport.getLength() + INNER_SVG_X_START;
        this.arrowYBottomMargin = carport.getWidth() + (INNER_SVG_Y_START * 1.5);
        this.arrowYTopMargin = INNER_SVG_Y_START * 0.75;

        carportTopSvg.addArrowDefs();
        addFrame();
        addBeams();
        addRafters();
        addArrows();
        addArrowText();
        //addPost();
        testAddPost();
        addMetalStrap();
        carportTopSvg.addSvg(carportInnerSvg);
    }

    private void addFrame()
    {
        carportInnerSvg.addRectangle(0,0,carport.getWidth(),carport.getLength(), BASE_STYLE);
    }

    private void addBeams()
    {
        carportInnerSvg.addRectangle(0, POST_EDGE_INSET_CM, RAFTER_WIDTH_CM, carport.getLength(), BASE_STYLE);
        carportInnerSvg.addRectangle(0,carport.getWidth() - POST_EDGE_INSET_CM, RAFTER_WIDTH_CM, carport.getLength(), BASE_STYLE);
    }

    private void testAddPost()
    {
        Shed shed = carport.getShed();
        if(shed != null)
        {
            addShedPosts(shed);
        }

        List<Double> postXpositions = PostPlacementCalculatorUtil.calculatePostPlacements(carport);

        postXpositions.forEach(postX -> addPostPair(postX));
    }

    private void addPost()
    {
        int totalNumberOfPost = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        int numberOfPostsPerRow = totalNumberOfPost / 2;

        double lastPostPosition = carport.getLength() - POST_OFFSET_END_POSITION_CM;

        Shed shed = carport.getShed();
        if(shed != null)
        {
            addShedPosts(shed);
        }

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

    private void addShedPosts(Shed shed)
    {
        double shedStartX = carport.getLength() - shed.getLength() - POST_OFFSET_END_POSITION_CM;
        double shedEndX = carport.getLength() - POST_OFFSET_END_POSITION_CM;

        switch (shed.getShedPlacement())
        {
            case FULL_WIDTH ->
            {
                double shedMiddleY = PostPlacementCalculatorUtil.calculatePostYPosition(carport);
                double shedMiddlePosition = shedMiddleY + INNER_SVG_Y_START;
                addPostPair(shedStartX);
                addShedPostPair(shedStartX, shedMiddlePosition, shedEndX);
            }

            case LEFT ->
            {

            }
        }
    }

    private void addPostPair(double x)
    {
        carportInnerSvg.addRectangle(x, POST_EDGE_INSET_CM - POST_VERTICAL_OFFSET_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(x, yPositionBottom, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
    }

    private void addShedPostPair(double shedStartX, double shedY, double shedEndX)
    {
        carportInnerSvg.addRectangle(shedStartX, shedY - POST_VERTICAL_OFFSET_CM, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(shedEndX, shedY, POST_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
    }

    private void addRafters()
    {
        RafterCalculationDTO rafterCalcDTO = PartCalculator.calculateRafters(carport.getLength(), RAFTER_WIDTH_CM);
        int numberOfRafters = rafterCalcDTO.numberOfRafters();
        double spacing = rafterCalcDTO.spacing();
        double currentXPos = 0;

        for(int i = 0; i < numberOfRafters ; i++)
        {
            carportInnerSvg.addRectangle(currentXPos, 0, carport.getWidth(), RAFTER_WIDTH_CM, BASE_STYLE);
            currentXPos += spacing;
        }
    }

    private void addMetalStrap()
    {
        double ySpacing = 5;
        double xMetalStrapStart = 0;
        double xMetalStrapEnd = 0;
        double yMetalStrapOffset = 5;
        double yMetalStrapPositionTop = yPositionTop - yMetalStrapOffset;
        double yMetalStrapPositionBottom = yPositionBottom + yMetalStrapOffset;

        if(carport.getShed() == null)
        {
            xMetalStrapEnd = carport.getLength();
        }
        else
        {
            double shedLengthWithPostOffSet = carport.getShed().getLength() + POST_OFFSET_END_POSITION_CM;
            xMetalStrapStart = MAX_SPACING_CM + RAFTER_WIDTH_CM;
            xMetalStrapEnd = (carport.getLength() - shedLengthWithPostOffSet) + POST_WIDTH_CM;
        }

        carportInnerSvg.addLine(xMetalStrapStart, yMetalStrapPositionTop, xMetalStrapEnd , yMetalStrapPositionBottom, DASHARRAY_STYLE);
        carportInnerSvg.addLine(xMetalStrapStart, yMetalStrapPositionTop + ySpacing, xMetalStrapEnd, yMetalStrapPositionBottom + ySpacing, DASHARRAY_STYLE);

        carportInnerSvg.addLine(xMetalStrapStart, yMetalStrapPositionBottom, xMetalStrapEnd, yMetalStrapPositionTop, DASHARRAY_STYLE);
        carportInnerSvg.addLine(xMetalStrapStart, yMetalStrapPositionBottom + ySpacing, xMetalStrapEnd, yMetalStrapPositionTop + ySpacing, DASHARRAY_STYLE);
    }

    private void addArrows()
    {
        double tickLength = 20;
        double tickLengthLeft = tickLength / 6.0;
        double xStartOffSet = RAFTER_WIDTH_CM / 2;

        RafterCalculationDTO rafterCalculationDTO = PartCalculator.calculateRafters(carport.getLength(),RAFTER_WIDTH_CM);
        int numberOfSpaces = rafterCalculationDTO.numberOfRafters() - 1;
        double spacing = rafterCalculationDTO.spacing();

        for(int i = 0; i < numberOfSpaces; i++)
        {
            double x1 = INNER_SVG_X_START + (i * spacing + xStartOffSet);
            double x2 = INNER_SVG_X_START + ((i + 1) * spacing + xStartOffSet);
            double y = arrowYTopMargin;

            carportTopSvg.addLineWithArrows(x1, y, x2, y);
            carportTopSvg.addLine(x1, y - tickLength/2, x1, y + tickLength/2, BASE_STYLE);
            carportTopSvg.addLine(x2, y - tickLength/2, x2, y + tickLength/2, BASE_STYLE);

            double midX = (x1 + x2) / 2.0;
            double spacing_meters = spacing / 100.0;
            carportTopSvg.addText(midX, y - 15, 0, String.format("%.2f", spacing_meters));
        }

        // Left arrow
        carportTopSvg.addLineWithArrows(arrowLeftXStart, INNER_SVG_Y_START, arrowLeftXStart, arrowBottomY);
        //Left arrow top measure line
        carportTopSvg.addLine(arrowLeftXStart - tickLengthLeft, INNER_SVG_Y_START, arrowInnerLeftXStart + tickLength, INNER_SVG_Y_START, BASE_STYLE);
        //Left arrow bottom measure line
        carportTopSvg.addLine(arrowLeftXStart - tickLengthLeft, arrowBottomY, arrowInnerLeftXStart + tickLength, arrowBottomY, BASE_STYLE);


        //Left inner arrow
        carportTopSvg.addLineWithArrows(arrowInnerLeftXStart, INNER_SVG_Y_START + POST_EDGE_INSET_CM, arrowInnerLeftXStart, arrowBottomY - POST_EDGE_INSET_CM);

        //Left inner measure line
        carportTopSvg.addLine(arrowInnerLeftXStart - tickLengthLeft, INNER_SVG_Y_START + POST_EDGE_INSET_CM, arrowInnerLeftXStart + tickLength, INNER_SVG_Y_START + POST_EDGE_INSET_CM, BASE_STYLE);
        carportTopSvg.addLine(arrowInnerLeftXStart - tickLengthLeft, arrowBottomY - POST_EDGE_INSET_CM, arrowInnerLeftXStart + tickLength, arrowBottomY - POST_EDGE_INSET_CM, BASE_STYLE);

        // Bottom arrow
        carportTopSvg.addLineWithArrows(INNER_SVG_X_START, arrowYBottomMargin, arrowBottomXEnd, arrowYBottomMargin);
        carportTopSvg.addLine(INNER_SVG_X_START, arrowYBottomMargin - tickLength, INNER_SVG_X_START, arrowYBottomMargin + tickLengthLeft, BASE_STYLE);
        carportTopSvg.addLine(arrowBottomXEnd, arrowYBottomMargin - tickLength, arrowBottomXEnd, arrowYBottomMargin + tickLengthLeft, BASE_STYLE);
    }

    private void addArrowText()
    {
        int innerMeasureArrowLength = (int)(carport.getWidth() - (2 * POST_EDGE_INSET_CM));
        int textOfSetFromArrow = 10;
        double arrowBottomTextCenter =  (INNER_SVG_X_START + arrowBottomXEnd) / 2;

        double carportInnerMeasurementInMeters = innerMeasureArrowLength / 100.0;
        double carportWidthInMeters = carport.getWidth() / 100.0;
        double carportLengthInMeters = carport.getLength() / 100.0;

        carportTopSvg.addText(arrowLeftXStart - textOfSetFromArrow, arrowBottomY / 2, -90, String.valueOf(carportWidthInMeters));
        carportTopSvg.addText(arrowBottomTextCenter, arrowYBottomMargin + (2 * textOfSetFromArrow), 0, String.valueOf(carportLengthInMeters));

        carportTopSvg.addText(arrowInnerLeftXStart - textOfSetFromArrow, arrowBottomY / 2, -90, String.valueOf(carportInnerMeasurementInMeters));
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