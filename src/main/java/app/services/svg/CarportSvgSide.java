package app.services.svg;

import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.entities.Shed;
import app.util.PartCalculator;

public class CarportSvgSide
{
    private Carport carport;
    private Svg carportSideSvg;
    private Svg carportInnerSvg;
    private final String VIEW_BOX = "0 0 1000 500";
    private final String WIDTH_SIZE = "100%";
    private final String BASE_STYLE = "stroke-width: 1px; stroke:#000000; fill: #ffffff";
    private final int CARPORT_TOP_HEIGHT_CM = 230;
    private final double RAFTER_HEIGHT_CM =  19.5;
    private final double WEATHER_BOARD_HEIGHT_CM = 19.5;
    private final double POST_WIDTH_CM = 10.0;
    private final double POST_HEIGHT_CM = 210.0 - RAFTER_HEIGHT_CM;
    private final double POST_START_POSITION_CM = 100.0;

    private double yPositionBottom;
    private double yPositionTop;
    private final int INNER_SVG_X_START = 150;
    private final int INNER_SVG_Y_START = 50;
    double arrowXLeftMargin;
    double arrowYEnd;
    double arrowXEnd;
    double arrowYBottomMargin;
    double arrowYTopMargin;

    // Verify
    private final double POST_VERTICAL_OFFSET_CM = 2.5;
    private final double POST_OFFSET_END_POSITION_CM = 30.00;
    private final double POST_SPACING_CM = 310;
    private final double MAX_SPACING_CM = 55.0;
    private final double POST_EDGE_INSET_CM = 35.00;




    public CarportSvgSide(Carport carport)
    {
        this.carport = carport;
        this.carportSideSvg = new Svg(0, 0, WIDTH_SIZE, VIEW_BOX);
        this.carportInnerSvg = new Svg(INNER_SVG_X_START, INNER_SVG_Y_START, carport.getLength(), CARPORT_TOP_HEIGHT_CM, getInnerViewBox(carport.getWidth(), carport.getLength()));
        this.yPositionBottom =  carport.getWidth() - POST_EDGE_INSET_CM - 2.5;
        this.yPositionTop = POST_EDGE_INSET_CM + 4.5;

        this.arrowXLeftMargin = INNER_SVG_X_START / 2;
        this.arrowYEnd = carport.getWidth() + INNER_SVG_Y_START;
        this.arrowXEnd = carport.getLength() + INNER_SVG_X_START;
        this.arrowYBottomMargin = carport.getWidth() + (INNER_SVG_Y_START * 1.5);
        this.arrowYTopMargin = INNER_SVG_Y_START * 0.75;

        carportSideSvg.addArrowDefs();
        addFrame();
        addPost();
        //addBeams();
        //addWeatherBoard();
        //addArrows();
        //addArrowText();


        carportSideSvg.addSvg(carportInnerSvg);
    }

    private void addFrame()
    {
        carportInnerSvg.addLine(0,CARPORT_TOP_HEIGHT_CM,carport.getLength(),CARPORT_TOP_HEIGHT_CM, BASE_STYLE);
        carportInnerSvg.addLine(0,RAFTER_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 10, + 50, BASE_STYLE);
        carportInnerSvg.addLine(carport.getLength(), RAFTER_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 20, CARPORT_TOP_HEIGHT_CM + 40, BASE_STYLE);
    }

    private void addBeams()
    {
        carportInnerSvg.addRectangle(0, POST_EDGE_INSET_CM, 4.5, carport.getLength() + POST_OFFSET_END_POSITION_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(0,carport.getWidth() - POST_EDGE_INSET_CM, 4.5, carport.getLength(), BASE_STYLE);
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
                double shedMiddleY = carport.getWidth() / 2;
                addPostPair(shedStartX);
                addShedPostPair(shedStartX, shedMiddleY, shedEndX);

            }
            case RIGHT ->
            {

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

    private void addWeatherBoard()
    {
        RafterCalculationDTO rafterCalcDTO = PartCalculator.calculateRafters(carport.getLength(), 4.5);
        int numberOfRafters = rafterCalcDTO.numberOfRafters();
        double spacing = rafterCalcDTO.spacing();
        double currentXPos = 0;

        for(int i = 0; i < numberOfRafters ; i++)
        {
            carportInnerSvg.addRectangle(currentXPos, 0, carport.getWidth(), 4.5, BASE_STYLE);
            currentXPos += spacing;
        }
    }

    private void addArrows()
    {
        double tickLength = 20;
        double xStartOffSet = 4.5 / 2;

        RafterCalculationDTO rafterCalculationDTO = PartCalculator.calculateRafters(carport.getLength(),4.5);
        int numberOfSpaces = rafterCalculationDTO.numberOfRafters() - 1;
        double spacing = rafterCalculationDTO.spacing();

        for(int i = 0; i < numberOfSpaces; i++)
        {
            double x1 = INNER_SVG_X_START + (i * spacing + xStartOffSet);
            double x2 = INNER_SVG_X_START + ((i + 1) * spacing + xStartOffSet);
            double y = arrowYTopMargin;

            carportSideSvg.addLineWithArrows(x1, y, x2, y);
            carportSideSvg.addLine(x1, y - tickLength/2, x1, y + tickLength/2, BASE_STYLE);
            carportSideSvg.addLine(x2, y - tickLength/2, x2, y + tickLength/2, BASE_STYLE);

            double midX = (x1 + x2) / 2.0;
            double spacing_meters = spacing / 100.0;
            carportSideSvg.addText(midX, y - 15, 0, String.format("%.2f", spacing_meters));
        }

        // Left arrow
        carportSideSvg.addLineWithArrows(arrowXLeftMargin, INNER_SVG_Y_START, arrowXLeftMargin, arrowYEnd);
        //Left inner arrow
        carportSideSvg.addLineWithArrows(INNER_SVG_X_START * 0.75, INNER_SVG_Y_START + POST_EDGE_INSET_CM, INNER_SVG_X_START * 0.75, arrowYEnd - POST_EDGE_INSET_CM);
        // Bottom arrow
        carportSideSvg.addLineWithArrows(INNER_SVG_X_START, arrowYBottomMargin, arrowXEnd, arrowYBottomMargin);
    }

    private void addArrowText()
    {
        carportSideSvg.addText(arrowXLeftMargin - 10, arrowYEnd / 2, -90, String.valueOf(carport.getWidth()));
        carportSideSvg.addText(arrowXEnd / 2, arrowYBottomMargin + 15, 0, String.valueOf(carport.getLength()));

        //TODO FIX hardcoding Inner text
        int innerArrowLength = (int)(carport.getWidth() - (2 * POST_EDGE_INSET_CM));
        carportSideSvg.addText(INNER_SVG_X_START * 0.75 - 10, arrowYEnd / 2, -90, String.valueOf(innerArrowLength));
    }

    private String getInnerViewBox(int width, int length)
    {
        String widthString = String.valueOf(width);
        String lengthString = String.valueOf(length);

        return "0 0 " + lengthString + " " + widthString;
    }
    @Override
    public String toString() {
        return carportSideSvg.toString();
    }
}