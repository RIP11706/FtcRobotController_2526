package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.UtilityOctoQuadConfigMenu;

@TeleOp(name = ": ShooterWithIntake V1", group = "Concept")

public class ShooterWithIntake extends LinearOpMode {

    // 1. Define the states for our state machine
    enum LaunchStateEnum {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING,
    }

    // Set the initial state
    private LaunchStateEnum launchState = LaunchStateEnum.IDLE;

    // Define constants for LauncherMotor power and ramp rate
    static final double RAMP_POWER_INCREMENT = 0.02;  // Power increment per cycle (was INCREMENT)
    static final long RAMP_CYCLE_MS = 50;           // Time in milliseconds per ramp cycle (was CYCLE_MS)
    static final double MAX_SPEED = 1.0;              // Maximum LauncherMotor speed (was MAX_FWD)

    // Declare OpMode members
    private DcMotorEx launcherMotor = null;
    public  double LAUNCHER_TARGET_VELOCITY = 1275;
    public  double LAUNCHER_MIN_VELOCITY = 1075;
    private double STOP_VELOCITY = 0;
    private double FEED_TIME_SECONDS = 0.4;

    private DcMotor feederMotor = null;
    private CRServo lift = null;

    private double currentMotorPower = 0.0;
// lift servo
    private DcMotor intakeMotor = null;
    private boolean IntakeOn = false;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    enum IntakeState {
        IntakeOff,
        Intake1,
        Intake2,
        Intake3
    }

    // Set the initial state
    private IntakeState currentIntakeState = IntakeState.IntakeOff;

    // Create a timer to manage the ramp rate
    private  ElapsedTime feederTimer = new ElapsedTime();

    @Override
    public void runOpMode() {

        // Initialize the hardware variables.
        // IMPORTANT: Make sure the LauncherMotor name "shooter_drive" matches your robot's configuration.
        launcherMotor = hardwareMap.get(DcMotorEx.class, "shooter_drive");
        // Optional: If the LauncherMotor runs backwards, uncomment the next line
        // LauncherMotor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.addData(">", "Press Start to begin");

        telemetry.update();
        // telemetry.update();
        //telemetry.speak("Six seven");

        // Map the motors to the names in the robot's configuration file
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        feederMotor = hardwareMap.get(DcMotor.class, "feeder_motor");




        // 3. Set LauncherMotor directions
        // Most robots need the motors on one side to be reversed to drive forward.
        // If your robot drives backwards when you push the joysticks forward,
        // reverse the directions here. For example, switch REVERSE to FORWARD.
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        // Optional: Set the motors to brake when power is zero.
        // This can help prevent the robot from drifting.
        leftFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Let the driver know initialization is complete.
        telemetry.addData("Status", "Initialized");
        telemetry.addData(">", "Press Start to drive");
        telemetry.update();


        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Reset the timer once the OpMode starts
        feederTimer.reset();
        // The main loop runs until the driver presses STOP
        while (opModeIsActive()) {

            // --- LAUNCHER ---

            // 2. Determine the next state based on trigger input
            updateStateFromInput();
            // 3. Execute logic based on the current state

            // 4. Set the LauncherMotor power
            launcherMotor.setPower(currentMotorPower);

            // 5. Provide telemetry for debugging
            telemetry.addData("State", launchState.toString());
            telemetry.addData("Launch Power", "%.2f", currentMotorPower);
            telemetry.addData("Right Trigger", "%.2f", gamepad1.right_trigger);
            telemetry.addData("Left Trigger", "%.2f", gamepad1.left_trigger);
            telemetry.addData("Feeder Power", "%.2f", feederMotor.getPower());
            telemetry.addData("Intake Power", "%.2f", intakeMotor.getPower());
            telemetry.update();

            // --- INTAKE ---
            switch (currentIntakeState) {
                case IntakeOff:
                    intakeMotor.setPower(0);
                    feederMotor.setPower(0);
                    break;
                case Intake1:
                    intakeMotor.setPower(-0.5);
                    feederMotor.setPower(-0.5);
                    break;
                case Intake2:
                    intakeMotor.setPower(-0.5);
                    feederMotor.setPower(-0.5);
                    break;
                case Intake3:
                    intakeMotor.setPower(0.3);
                    break;


            }

            if (gamepad1.xWasPressed()) {
                IntakeOn = !IntakeOn;
            }

            if (IntakeOn) {
                currentIntakeState = IntakeState.Intake1;
            } else if (gamepad1.a){
               currentIntakeState = IntakeState.Intake3;
            } else {
                currentIntakeState = IntakeState.IntakeOff;
            }



            // --- DRIVETRAIN ---

            // 5. Get joystick values from gamepad 1
            // The Y-axis of the joysticks is inverted (pushing forward gives a negative value).
            // We negate the values to make forward positive.
            double y = gamepad1.left_stick_y;
            double x = -gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_y;

            // 6. Calculate the power for each LauncherMotor
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            // 6. Set the power for each LauncherMotor
            // The left joystick controls the left motors, and the right joystick controls the right motors.
            double frontleftPower = (y + x + rx) / denominator;
            double frontrightPower = (y - x - rx) / denominator;
            double backleftPower = (y - x + rx) / denominator;
            double backrightPower = (y + x - rx) / denominator;



            leftFrontDrive.setPower(frontleftPower);
            leftBackDrive.setPower(frontrightPower);
            rightFrontDrive.setPower(backleftPower);
            rightBackDrive.setPower(backrightPower);

            // 7. Add telemetry for debugging



        }

        // Stop the LauncherMotor when the OpMode ends
        launcherMotor.setPower(0);
    }

    /**
     * Reads gamepad input and updates the currentState variable.
     * This method centralizes the logic for state transitions.
     */
    private void updateStateFromInput() {
        // Read trigger values using a threshold to avoid jitter from sensitive triggers
        boolean rightTriggerPressed = gamepad1.right_trigger > 0.1;
        boolean leftTriggerPressed = gamepad1.left_trigger > 0.1;

        // Safety check: If both triggers are pressed, force a ramp down.
        if (rightTriggerPressed && leftTriggerPressed) {
            launchState = LaunchStateEnum.LAUNCH;
        }
        // If only the right trigger is pressed, ramp up.
        else if (rightTriggerPressed) {
            launchState = LaunchStateEnum.SPIN_UP;
        }
        // If only the left trigger is pressed, ramp down.
        else if (leftTriggerPressed) {
            launchState = LaunchStateEnum.LAUNCH;
        }
        // If no triggers are pressed, hold a steady speed.
        else {
            launchState = LaunchStateEnum.IDLE;
        }
        if (gamepad1.y) {
            launcherMotor.setVelocity(LAUNCHER_TARGET_VELOCITY) ;
        }
        else if (gamepad1.b) {
            launcherMotor.setVelocity(STOP_VELOCITY);
        }
    }
    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) {
                    // In the STEADY state, the LauncherMotor power does not change automatically.
                    // It holds its last value. We can still reset the timer to be ready.
                    launchState = LaunchStateEnum.SPIN_UP;
                }
                break;
            case SPIN_UP:
                launcherMotor.setVelocity(LAUNCHER_TARGET_VELOCITY);
                if (launcherMotor.getVelocity() > LAUNCHER_MIN_VELOCITY){
                    launchState = LaunchStateEnum.LAUNCH;
                }
                break;

            case LAUNCH:
                //lift the launch servo
                feederTimer.reset();
                launchState = LaunchStateEnum.LAUNCHING;
                break;

            case LAUNCHING:
                if (feederTimer.seconds() > FEED_TIME_SECONDS) {
                    launchState = LaunchStateEnum.IDLE;
                    // lift servo down
                }
        }
    }

}


