package app.dto;

import app.entities.Material;
import app.entities.MaterialLine;

import java.util.List;

public record BeamCalculationDTO(
        int numberOfBeams,
        List<Material> beamMaterials
)
{
}
