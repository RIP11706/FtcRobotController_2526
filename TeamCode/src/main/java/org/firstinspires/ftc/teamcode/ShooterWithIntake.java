package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class ShooterWithIntake extends LinearOpMode {

    // 1. Define the states for our state machine
    enum RampState {
        STEADY,
        RAMPING_UP,
        RAMPING_DOWN
    }

    // Set the initial state
    private RampState currentState = RampState.STEADY;

    // Define constants for LauncherMotor power and ramp rate
    static final double RAMP_POWER_INCREMENT = 0.02;  // Power increment per cycle (was INCREMENT)
    static final long RAMP_CYCLE_MS = 50;           // Time in milliseconds per ramp cycle (was CYCLE_MS)
    static final double MAX_SPEED = 1.0;              // Maximum LauncherMotor speed (was MAX_FWD)

    // Declare OpMode members
    private DcMotor LauncherMotor = null;

    private double currentMotorPower = 0.0;

    private DcMotor Intakemotor;
    private boolean IntakeOn = false;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;


    // Create a timer to manage the ramp rate
    private final ElapsedTime rampTimer = new ElapsedTime();

    @Override
    public void runOpMode() {

        // Initialize the hardware variables.
        // IMPORTANT: Make sure the LauncherMotor name "shooter_drive" matches your robot's configuration.
        LauncherMotor = hardwareMap.get(DcMotor.class, "shooter_drive");
        Intakemotor = hardwareMap.get(DcMotor.class, "intake_motor");
        // Optional: If the LauncherMotor runs backwards, uncomment the next line
        // LauncherMotor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.addData(">", "Press Start to begin");

        telemetry.update();
        telemetry.update();
        telemetry.speak("Six seven");

        // Map the motors to the names in the robot's configuration file
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

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
        rampTimer.reset();

        // The main loop runs until the driver presses STOP
        while (opModeIsActive()) {

            // --- LAUNCHER ---

            // 2. Determine the next state based on trigger input
            updateStateFromInput();
            // 3. Execute logic based on the current state
            switch (currentState) {
                case RAMPING_UP:
                    // If enough time has passed, increment power
                    if (rampTimer.milliseconds() > RAMP_CYCLE_MS) {
                        currentMotorPower += RAMP_POWER_INCREMENT;
                        // Clamp the power to the maximum speed
                        if (currentMotorPower > MAX_SPEED) {
                            currentMotorPower = MAX_SPEED;
                        }
                        rampTimer.reset(); // Reset the timer for the next increment
                    }
                    break;

                case RAMPING_DOWN:
                    // If enough time has passed, decrement power
                    if (rampTimer.milliseconds() > RAMP_CYCLE_MS) {
                        currentMotorPower -= RAMP_POWER_INCREMENT;
                        // Clamp the power to zero (LauncherMotor only runs forward in this design)
                        if (currentMotorPower < 0) {
                            currentMotorPower = 0;
                        }
                        rampTimer.reset(); // Reset the timer for the next decrement
                    }
                    break;

                case STEADY:
                    // In the STEADY state, the LauncherMotor power does not change automatically.
                    // It holds its last value. We can still reset the timer to be ready.
                    rampTimer.reset();
                    break;
            }
            // 4. Set the LauncherMotor power
            LauncherMotor.setPower(currentMotorPower);

            // 5. Provide telemetry for debugging
            telemetry.addData("State", currentState.toString());
            telemetry.addData("Motor Power", "%.2f", currentMotorPower);
            telemetry.addData("Right Trigger", "%.2f", gamepad1.right_trigger);
            telemetry.addData("Left Trigger", "%.2f", gamepad1.left_trigger);
            telemetry.update();

            // --- INTAKE ---
            if (gamepad1.xWasPressed()) {
                IntakeOn = !IntakeOn;
            }

            if (IntakeOn) {
                Intakemotor.setPower(0.67676767676767676767676767);
            } else {
                Intakemotor.setPower(0);
            }

            if (gamepad1.a){
                Intakemotor.setPower(-Intakemotor.getPower());
            }

            // --- DRIVETRAIN ---

            // 5. Get joystick values from gamepad 1
            // The Y-axis of the joysticks is inverted (pushing forward gives a negative value).
            // We negate the values to make forward positive.
            double leftPower = -gamepad1.left_stick_y;
            double rightPower = -gamepad1.right_stick_y;
            // 6. Set the power for each LauncherMotor
            // The left joystick controls the left motors, and the right joystick controls the right motors.
            leftFrontDrive.setPower(leftPower);
            leftBackDrive.setPower(leftPower);
            rightFrontDrive.setPower(rightPower);
            rightBackDrive.setPower(rightPower);

            // 7. Add telemetry for debugging
            telemetry.addData("Status", "Running");
            telemetry.addData("Left Power", "%.2f", leftPower);
            telemetry.addData("Right Power", "%.2f", rightPower);
            telemetry.update();

        }

        // Stop the LauncherMotor when the OpMode ends
        LauncherMotor.setPower(0);
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
            currentState = RampState.RAMPING_DOWN;
        }
        // If only the right trigger is pressed, ramp up.
        else if (rightTriggerPressed) {
            currentState = RampState.RAMPING_UP;
        }
        // If only the left trigger is pressed, ramp down.
        else if (leftTriggerPressed) {
            currentState = RampState.RAMPING_DOWN;
        }
        // If no triggers are pressed, hold a steady speed.
        else {
            currentState = RampState.STEADY;
        }
    }
}


