package org.firstinspires.ftc.teamcode.Modules.Intake;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.Math.AsymmetricMotionProfile;
import org.firstinspires.ftc.teamcode.Robot.Hardware;
import org.firstinspires.ftc.teamcode.Robot.IRobotModule;
import org.firstinspires.ftc.teamcode.Robot.IStateBasedModule;
import org.firstinspires.ftc.teamcode.Wrappers.CoolMotor;
import org.firstinspires.ftc.teamcode.Wrappers.Encoder;

@Config
public class Extendo implements IStateBasedModule, IRobotModule {

    public static boolean ENABLED = true;

    private final CoolMotor motor;
    public static boolean motorReversed = true;
    public final Encoder encoder;
    public static boolean encoderReversed = true;

    public static int zeroPos;
    public static int extendedPos;
    public static double extensionRate = 3000, extensionLimit = 1320;

    public static double resetPower = -0.5, velocityThreshold = 0, positionThreshold = 5;

    public static PIDFCoefficients pidf = new PIDFCoefficients(0.035,0,0.0004,0);

    public enum State{
        IN(0), RESETTING(0, IN), GOING_IN(0, RESETTING), OUT(extendedPos), GOING_OUT(extendedPos, OUT);

        public int position;
        public final State nextState;

        State(int position){
            this.position = position;
            this.nextState = this;
        }

        State(int position, State nextState){
            this.position = position;
            this.nextState = nextState;
        }
    }

    private State state;

    public State getState(){
        return state;
    }

    public void setState(State newState){
        updateStateValues();
        if(state == newState) return;
        this.state = newState;
    }

    private void updateStateValues(){
        State.OUT.position = extendedPos;
        State.GOING_OUT.position = extendedPos;
    }

    public Extendo(Hardware hardware, State initialState){
        if(!ENABLED) motor = null;
        else motor = new CoolMotor(hardware.mch3, CoolMotor.RunMode.PID, motorReversed);

        if(!ENABLED) encoder = null;
        else {
            encoder = hardware.ech2;
            if(encoderReversed) encoder.setDirection(Encoder.Direction.REVERSE);
        }

        this.state = initialState;
    }

    @Override
    public void update() {
        if(!ENABLED) return;

        updateStateValues();
        updateState();
        updateHardware();
    }

    @Override
    public void updateState() {
        if(state == State.RESETTING){
            if(Math.abs(encoder.getRawVelocity()) <= velocityThreshold){
                zeroPos = encoder.getCurrentPosition();
                updateStateValues();
                state = state.nextState;
            }
        }
        else if(Math.abs((state.position + zeroPos) - encoder.getCurrentPosition()) <= positionThreshold)
            state = state.nextState;
    }

    public int target = 0;

    @Override
    public void updateHardware() {
        if(state == State.RESETTING){
            motor.setMode(CoolMotor.RunMode.RUN);
            motor.setPower(resetPower);
        }else{
            target = state.position + zeroPos;
            motor.setMode(CoolMotor.RunMode.PID);
            motor.setPIDF(pidf, pidf.f * Math.signum(target - encoder.getCurrentPosition()));
            motor.calculatePower(encoder.getCurrentPosition(), target);
        }
        motor.update();

    }
}
