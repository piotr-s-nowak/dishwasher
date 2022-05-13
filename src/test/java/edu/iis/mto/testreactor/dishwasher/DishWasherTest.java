package edu.iis.mto.testreactor.dishwasher;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.engine.EngineException;
import edu.iis.mto.testreactor.dishwasher.pump.PumpException;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishWasherTest {

    @Mock
    private Engine engine;
    @Mock
    private DirtFilter dirtFilter;
    @Mock
    private Door door;
    @Mock
    private WaterPump waterPump;

    private DishWasher dishWasher;
    private WashingProgram anyEcoProgram;
    private FillLevel anyFillLevel;

    private static final double VALUE_GRATER_THAN_MAX_CAPACITY = 60.0;

    @BeforeEach
    public void setUp() {
        anyEcoProgram = WashingProgram.ECO;
        anyFillLevel = FillLevel.HALF;
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
    }

    @Test
    public void shouldThrowNPEWhenOneOfArgumentIsNull() {
        assertThrows(NullPointerException.class, () -> new DishWasher(null, engine, dirtFilter, null));
    }

    @Test
    public void shouldThrowNPEWhenProgramIsNull() {
        DishWasher dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
        assertThrows(NullPointerException.class, () -> dishWasher.start(null));
    }

    @Test
    public void shouldSetStatusDoorOpenWhenDoorIsUnlocked() {
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(anyEcoProgram)
                .withFillLevel(anyFillLevel)
                .withTabletsUsed(true)
                .build();
        when(door.closed()).thenReturn(false);
        assertEquals(Status.DOOR_OPEN, dishWasher.start(programConfiguration).getStatus());
    }

    @Test
    public void shouldSetStatusErrorFilterWhenDirtFilterCapacityIsWrong() {
        double valueLessThanMaxCapacity = 10.0;
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(anyEcoProgram)
                .withFillLevel(anyFillLevel)
                .withTabletsUsed(true)
                .build();
        when(door.closed()).thenReturn(true);
        when(dirtFilter.capacity()).thenReturn(valueLessThanMaxCapacity);
        assertEquals(Status.ERROR_FILTER, dishWasher.start(programConfiguration).getStatus());
    }

    @Test
    public void shouldCatchPumpExceptionAndSetStatusErrorPump() throws PumpException {
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(anyEcoProgram)
                .withFillLevel(anyFillLevel)
                .withTabletsUsed(true)
                .build();
        when(door.closed()).thenReturn(true);
        when(dirtFilter.capacity()).thenReturn(VALUE_GRATER_THAN_MAX_CAPACITY);
        doThrow(new PumpException()).when(waterPump).pour(any(FillLevel.class));
        assertEquals(Status.ERROR_PUMP, dishWasher.start(programConfiguration).getStatus());
    }

    @Test
    public void shouldCatchEngineExceptionAndSetStatusErrorProgram() throws EngineException {
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(anyEcoProgram)
                .withFillLevel(anyFillLevel)
                .withTabletsUsed(true)
                .build();
        when(door.closed()).thenReturn(true);
        when(dirtFilter.capacity()).thenReturn(VALUE_GRATER_THAN_MAX_CAPACITY);
        doThrow(new EngineException()).when(engine).runProgram(anyList());
        assertEquals(Status.ERROR_PROGRAM, dishWasher.start(programConfiguration).getStatus());
    }

}