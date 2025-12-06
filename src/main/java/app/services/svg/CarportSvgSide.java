package app.services.svg;

import app.dto.RafterCalculationDTO;
import app.entities.Carport;
import app.util.PartCalculator;

public class CarportSvgSide
{
    private Carport carport;
    private Svg carportSideSvg;
    private Svg carportInnerSvg;
    private final String VIEW_BOX = "0 0 1000 400";
    private final String WIDTH_SIZE = "100%";
    private final String BASE_STYLE = "stroke-width: 1px; stroke:#000000; fill: #ffffff";
    private final int CARPORT_TOP_HEIGHT_FRONT_CM = 230;
    private final int CARPORT_TOP_HEIGHT_REAR_CM = 220;
    private final double CARPORT_HEIGHT_OFFSET_TO_TOP_BEAM = 20;
    private final double BEAM_HEIGHT_CM = 19.5;
    private final double WEATHER_BOARD_HEIGHT_CM = 15.0;
    private final double POST_WIDTH_CM = 10.0;
    private final double FACADE_CLADDING_BOARD_WIDTH = 10.0;
    private final double POST_HEIGHT_CM = 210;
    private final double POST_WITH_BEAM_CUTOUT_HEIGHT_CM = 210.0 - BEAM_HEIGHT_CM;
    private final double SPACE_BETWEEN_WEATHERBOARD_AND_BEAM_CM = 3.0;
    private final double RAFTER_WIDTH_CM = 4.5;
    private final double RAFTER_TOP_OFFSET_CM = 22.0;
    private final double ROOF_DROP_CM = 10.0;
    private final double POST_START_POSITION_CM = 100.0;

    private double yPositionBottom;
    private double yPositionTop;
    private final int INNER_SVG_X_START = 150;
    private final int INNER_SVG_Y_START = 50;
    double arrowLeftXStart;
    double arrowInnerLeftXStart;
    double arrowRightXStart;
    double arrowBottomY;
    double arrowBottomXEnd;
    double arrowYBottomMargin;
    double arrowYTopMargin;

    // Verify
    private final double POST_MAX_SPAN_CM = 310.0;
    private final double POST_FRONT_PLACEMENT_CM = 100.0;
    private final double POST_CENTER_PLACEMENT_CM = POST_FRONT_PLACEMENT_CM + POST_MAX_SPAN_CM;
    private double POST_BACK_PLACEMENT_CM;
    private final double MAX_SPACING_CM = 55.0;
    private final double POST_EDGE_INSET_CM = 35.00;


    public CarportSvgSide(Carport carport)
    {
        this.carport = carport;
        this.carportSideSvg = new Svg(0, 0, WIDTH_SIZE, VIEW_BOX);
        this.carportInnerSvg = new Svg(INNER_SVG_X_START, INNER_SVG_Y_START, carport.getLength(), CARPORT_TOP_HEIGHT_FRONT_CM, getInnerViewBox(carport.getWidth(), carport.getLength()));
        this.yPositionBottom = CARPORT_TOP_HEIGHT_FRONT_CM;
        this.yPositionTop = 0;
        this.arrowBottomY = CARPORT_TOP_HEIGHT_FRONT_CM + INNER_SVG_Y_START;
        this.arrowBottomXEnd = carport.getLength() + INNER_SVG_X_START;
        this.arrowLeftXStart = INNER_SVG_X_START / 2;
        this.arrowInnerLeftXStart = INNER_SVG_X_START * 0.75;
        this.arrowRightXStart = arrowBottomXEnd * 1.04;
        this.arrowYBottomMargin = CARPORT_TOP_HEIGHT_FRONT_CM + (INNER_SVG_Y_START * 1.5);
        this.arrowYTopMargin = INNER_SVG_Y_START * 0.75;
        this.POST_BACK_PLACEMENT_CM = carport.getLength() - 30.0;

        carportSideSvg.addArrowDefs();
        addFrame();
        addPost();
        //addFacadeCladding();
        addFacadeCladding();
        addRafters();
        addBeamAndWeatherBoard();
        addArrows();
        addArrowText();
        carportSideSvg.addSvg(carportInnerSvg);
    }

    private void addFrame()
    {
        double measureLineFront = CARPORT_TOP_HEIGHT_FRONT_CM - (BEAM_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 10);
        double measureLineBack = CARPORT_TOP_HEIGHT_FRONT_CM - (BEAM_HEIGHT_CM + WEATHER_BOARD_HEIGHT_CM + 20);

        carportInnerSvg.addLine(0, yPositionBottom, carport.getLength(), CARPORT_TOP_HEIGHT_FRONT_CM, BASE_STYLE);
        carportInnerSvg.addLine(0, yPositionBottom, 0, measureLineFront, BASE_STYLE);
        carportInnerSvg.addLine(carport.getLength(), yPositionBottom, carport.getLength(), measureLineBack, BASE_STYLE);
    }

    private void addRafters()
    {
        double rafterWidth = RAFTER_WIDTH_CM;
        double rafterHeight = BEAM_HEIGHT_CM;

        RafterCalculationDTO rafterCalcDTO = PartCalculator.calculateRafters(carport.getLength(), rafterWidth);
        int numberOfRafters = rafterCalcDTO.numberOfRafters();

        double spacing = rafterCalcDTO.spacing();
        double rafterYPoint = WEATHER_BOARD_HEIGHT_CM + SPACE_BETWEEN_WEATHERBOARD_AND_BEAM_CM + BEAM_HEIGHT_CM - RAFTER_TOP_OFFSET_CM;
        double currentXPos = 0;

        for (int i = 0; i < numberOfRafters; i++)
        {
            carportInnerSvg.addRectangle(currentXPos, rafterYPoint, rafterHeight, rafterWidth, BASE_STYLE);
            currentXPos += spacing;
        }
    }


    private void addPost()
    {
        int totalNumberOfPost = PartCalculator.calculateNumberOfPostsWithOutShed(carport.getLength());
        int numberOfPostsPerRow = totalNumberOfPost / 2;

        carportInnerSvg.addRectangle(POST_FRONT_PLACEMENT_CM, yPositionBottom - POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        carportInnerSvg.addRectangle(POST_BACK_PLACEMENT_CM, yPositionBottom - POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);

        if (numberOfPostsPerRow == 3)
        {
            carportInnerSvg.addRectangle(POST_CENTER_PLACEMENT_CM, yPositionBottom - POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WITH_BEAM_CUTOUT_HEIGHT_CM, POST_WIDTH_CM, BASE_STYLE);
        }
    }

    private void addBeamAndWeatherBoard()
    {
        double weatherBoardX1 = 0.0;
        double weatherBoardX2 = carport.getLength();
        double weatherBoardY1 = yPositionBottom - CARPORT_TOP_HEIGHT_FRONT_CM;
        double weatherBoardY2 = yPositionBottom - CARPORT_TOP_HEIGHT_REAR_CM;

        double beamX1 = 0.0;
        double beamX2 = carport.getLength();
        double beamY1 = yPositionBottom - POST_HEIGHT_CM;
        double beamY2 = (yPositionBottom - POST_HEIGHT_CM) + ROOF_DROP_CM;

        addBeamOrWeatherBoard(weatherBoardX1, weatherBoardY1, weatherBoardX2, weatherBoardY2, WEATHER_BOARD_HEIGHT_CM); // Weather board
        addBeamOrWeatherBoard(beamX1, beamY1, beamX2, beamY2, BEAM_HEIGHT_CM); // Beam
    }

    private void addArrows()
    {
        double tickLength = 20;
        double tickLengthLeft = tickLength / 6.0;
        double xStartOffSet = 4.5 / 2;
        double innerArrowTopY = INNER_SVG_Y_START + CARPORT_HEIGHT_OFFSET_TO_TOP_BEAM;
        double rightArrowTopY = INNER_SVG_Y_START + ROOF_DROP_CM;

        RafterCalculationDTO rafterCalculationDTO = PartCalculator.calculateRafters(carport.getLength(), 4.5);
        int numberOfSpaces = rafterCalculationDTO.numberOfRafters() - 1;
        double spacing = rafterCalculationDTO.spacing();

        for (int i = 0; i < numberOfSpaces; i++)
        {
            double x1 = INNER_SVG_X_START + (i * spacing + xStartOffSet);
            double x2 = INNER_SVG_X_START + ((i + 1) * spacing + xStartOffSet);
            double y = arrowYTopMargin;

            carportSideSvg.addLineWithArrows(x1, y, x2, y);
            carportSideSvg.addLine(x1, y - tickLength / 2, x1, y + tickLength / 2, BASE_STYLE);
            carportSideSvg.addLine(x2, y - tickLength / 2, x2, y + tickLength / 2, BASE_STYLE);

            double midX = (x1 + x2) / 2.0;
            double spacing_meters = spacing / 100.0;
            carportSideSvg.addText(midX, y - 15, 0, String.format("%.2f", spacing_meters));
        }

        // Left(First) arrow
        carportSideSvg.addLineWithArrows(arrowLeftXStart, INNER_SVG_Y_START, arrowLeftXStart, arrowBottomY);
        //Top measure line
        carportSideSvg.addLine(arrowLeftXStart - tickLength, INNER_SVG_Y_START, arrowInnerLeftXStart + tickLength, INNER_SVG_Y_START, BASE_STYLE);
        //Bottom measure line
        carportSideSvg.addLine(arrowLeftXStart - tickLength, arrowBottomY, arrowInnerLeftXStart + tickLength, arrowBottomY, BASE_STYLE);

        //Left inner arrow
        carportSideSvg.addLineWithArrows(arrowInnerLeftXStart, innerArrowTopY, arrowInnerLeftXStart, arrowBottomY);
        //Left inner measure line
        carportSideSvg.addLine(arrowInnerLeftXStart - tickLength, innerArrowTopY, arrowInnerLeftXStart + tickLength, innerArrowTopY, BASE_STYLE);

        // Bottom arrow
        carportSideSvg.addLineWithArrows(INNER_SVG_X_START, arrowYBottomMargin, arrowBottomXEnd, arrowYBottomMargin);
        carportSideSvg.addLine(INNER_SVG_X_START, arrowYBottomMargin - tickLength, INNER_SVG_X_START, arrowYBottomMargin + tickLengthLeft, BASE_STYLE);
        carportSideSvg.addLine(arrowBottomXEnd, arrowYBottomMargin - tickLength, arrowBottomXEnd, arrowYBottomMargin + tickLengthLeft, BASE_STYLE);

        // Right(End) arrow
        carportSideSvg.addLineWithArrows(arrowRightXStart, rightArrowTopY, arrowRightXStart, arrowBottomY);
        //Top measure line
        carportSideSvg.addLine(arrowRightXStart - (tickLength * 1.5), rightArrowTopY, arrowRightXStart + (tickLength / 4), rightArrowTopY, BASE_STYLE);
        //Bottom measure line
        carportSideSvg.addLine(arrowRightXStart - (tickLength * 1.5), arrowBottomY, arrowRightXStart + (tickLength / 4), arrowBottomY, BASE_STYLE);
    }

    private void addArrowText()
    {
        int carportHeightToBeamBottom = 20;
        int innerArrowLength = CARPORT_TOP_HEIGHT_FRONT_CM - carportHeightToBeamBottom;
        double midY = (INNER_SVG_Y_START + arrowBottomY) / 2.0;
        double midX = (INNER_SVG_X_START + arrowBottomXEnd) / 2.0;
        int textOffSet = 10;

        double carportHeightInMeters = CARPORT_TOP_HEIGHT_FRONT_CM / 100.0;
        double carportHeightToBeamBottomInMeters = innerArrowLength / 100.0;
        double carportEndHeightInMeters = CARPORT_TOP_HEIGHT_REAR_CM / 100.0;
        double carportLengthInMeters = carport.getLength() / 100.0;

        //Left arrow
        carportSideSvg.addText(arrowLeftXStart - textOffSet, midY, -90, String.valueOf(carportHeightInMeters));

        //Left inner arrow
        carportSideSvg.addText(INNER_SVG_X_START * 0.75 - textOffSet, midY, -90, String.valueOf(carportHeightToBeamBottomInMeters));

        //Right arrow
        carportSideSvg.addText(arrowRightXStart - textOffSet, midY, -90, String.valueOf(carportEndHeightInMeters));

        //Bottom arrow
        carportSideSvg.addText(midX, arrowYBottomMargin + 15, 0, String.valueOf(carportLengthInMeters));
    }

    private void addBeamOrWeatherBoard(double x1, double y1, double x2, double y2, double offset)
    {
        String points = String.format("%.1f,%.1f %.1f,%.1f %.1f,%.1f %.1f,%.1f",
                x1, y1,             // Top left
                x2, y2,             // Top right
                x2, y2 + offset,    // Bottom right
                x1, y1 + offset);   // Bottom left

        carportInnerSvg.addPolygon(points, BASE_STYLE);
    }

    private void addFacadeCladding()
    {
        if (carport.getShed() == null)
        {
            return;
        }

        final double bottomBoardWidth = FACADE_CLADDING_BOARD_WIDTH;
        final double topBoardWidth = 5.0;
        final double carportEndOffSet = 30.0;

        double totalShedLengthWithEndOffSet = carport.getShed().getLength() + carportEndOffSet;
        double facadeCladdingStartX = carport.getLength() - totalShedLengthWithEndOffSet;
        double facadeCladdingEndX = POST_BACK_PLACEMENT_CM;
        double maxHeight = POST_WITH_BEAM_CUTOUT_HEIGHT_CM;
        double startY = yPositionBottom - maxHeight;

        double currentXPos = facadeCladdingStartX;
        int boardCounter = 0;

        while (currentXPos < facadeCladdingEndX)
        {
            double boardWidthToDraw;

            if (boardCounter % 2 == 0)
            {
                boardWidthToDraw = bottomBoardWidth;
            }
            else
            {
                boardWidthToDraw = topBoardWidth;
            }

            double remainingLength = facadeCladdingEndX - currentXPos;

            if (boardWidthToDraw > remainingLength)
            {
                boardWidthToDraw = remainingLength;
            }

            carportInnerSvg.addRectangle(currentXPos, startY, maxHeight, boardWidthToDraw, BASE_STYLE);
            currentXPos += boardWidthToDraw;
            boardCounter++;

            if (boardWidthToDraw <= 0)
            {
                break;
            }
        }
    }

    private String getInnerViewBox(int height, int length)
    {
        String heightString = String.valueOf(CARPORT_TOP_HEIGHT_FRONT_CM);
        String lengthString = String.valueOf(length);

        return "0 0 " + lengthString + " " + heightString;
    }
    @Override
    public String toString() {
        return carportSideSvg.toString();
    }
}