package org.firstinspires.ftc.teamcode.Modules.Intake;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot.Hardware;
import org.firstinspires.ftc.teamcode.Robot.IRobotModule;
import org.firstinspires.ftc.teamcode.Robot.IStateBasedModule;
import org.firstinspires.ftc.teamcode.Wrappers.CoolServo;

@Config
public class Ramp implements IStateBasedModule, IRobotModule {

    public static boolean ENABLED = true;

    private final CoolServo servo;
    public static boolean reversedServo = true;

    public static double rampPosition = 0.5, upPosition = 0.6;
    public static int index = 0;
    public static double pos0 = 0.37, pos1 =0.37, pos2 = 0.4, pos3 = 0.5, pos4 = 0.58;

    public enum State{
        UP(upPosition), INTAKE(rampPosition);

        public double position;
        public final State nextState;

        State(double position){
            this.position = position;
            this.nextState = this;
        }

        State(double position, State nextState){
            this.position = position;
            this.nextState = nextState;
        }
    }

    private void updateStateValues(){

        double[] poses = new double[]{pos0, pos1, pos2, pos3, pos4};
        rampPosition = poses[index];

        State.UP.position = upPosition;
        State.INTAKE.position = rampPosition;
    }

    private State state;

    private final ElapsedTime timer = new ElapsedTime();

    public State getState(){
        return state;
    }

    public void setState(State newState){
        if(newState == state) return;
        this.state = newState;
        timer.reset();
    }

    public Ramp(Hardware hardware, State initialState){
        if(!ENABLED) servo = null;
        else servo = new CoolServo(hardware.sch1, reversedServo, initialState.position);
        timer.startTime();
        setState(initialState);
        if(ENABLED) servo.forceUpdate();
    }

    @Override
    public void initUpdate() {
        update();
    }

    @Override
    public void update() {
        if(!ENABLED) return;

        updateStateValues();
        updateHardware();
        updateState();
    }

    @Override
    public void updateState() {

    }

    @Override
    public void updateHardware() {
        servo.setPosition(state.position);

        servo.update();
    }
}
