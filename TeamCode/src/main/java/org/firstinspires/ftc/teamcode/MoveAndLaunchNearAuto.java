package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit.INCH;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.FEED_TIME_SECONDS;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.INTAKE_POSITION;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.LAUNCHER_MIN_VELOCITY;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.LAUNCHER_TARGET_VELOCITY;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.LAUNCH_POSITION;
import static org.firstinspires.ftc.teamcode.ShooterWithIntake.LIFT_TIME_SECONDS;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorGoBildaPinpoint;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@Autonomous
public class MoveAndLaunchNearAuto extends LinearOpMode {

    GoBildaPinpointDriver pinpoint;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    private DcMotorEx launcherMotor = null;
    private DcMotor feederMotor = null;
    private Servo liftServo = null;

    // lift servo
    private DcMotor intakeMotor = null;
    ElapsedTime feederTimer = new ElapsedTime(ElapsedTime.SECOND_IN_NANO);
    enum LaunchStateEnum {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING,
    }

    // Set the initial state
    private ShooterWithIntake.LaunchStateEnum launchState = ShooterWithIntake.LaunchStateEnum.IDLE;

    @Override
    public void runOpMode() throws InterruptedException {


        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        /*
         *  Set the odometry pod positions relative to the point that you want the position to be measured from.
         *
         *  The X pod offset refers to how far sideways from the tracking point the X (forward) odometry pod is.
         *  Left of the center is a positive number, right of center is a negative number.
         *
         *  The Y pod offset refers to how far forwards from the tracking point the Y (strafe) odometry pod is.
         *  Forward of center is a positive number, backwards is a negative number.
         */
        pinpoint.setOffsets(-84.0, -168.0, DistanceUnit.MM); //these are tuned for 3110-0002-0001 Product Insight #1

        /*
         * Set the kind of pods used by your robot. If you're using goBILDA odometry pods, select either
         * the goBILDA_SWINGARM_POD, or the goBILDA_4_BAR_POD.
         * If you're using another kind of odometry pod, uncomment setEncoderResolution and input the
         * number of ticks per unit of your odometry pod.  For example:
         *     pinpoint.setEncoderResolution(13.26291192, DistanceUnit.MM);
         */
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        /*
         * Set the direction that each of the two odometry pods count. The X (forward) pod should
         * increase when you move the robot forward. And the Y (strafe) pod should increase when
         * you move the robot to the left.
         */
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);

        /*
         * Before running the robot, recalibrate the IMU. This needs to happen when the robot is stationary
         * The IMU will automatically calibrate when first powered on, but recalibrating before running
         * the robot is a good idea to ensure that the calibration is "good".
         * resetPosAndIMU will reset the position to 0,0,0 and also recalibrate the IMU.
         * This is recommended before you run your autonomous, as a bad initial calibration can cause
         * an incorrect starting value for x, y, and heading.
         */
        pinpoint.resetPosAndIMU();

        // Set the location of the robot - this should be the place you are starting the robot from
        pinpoint.setPosition(new Pose2D(INCH, 0, 0, AngleUnit.DEGREES, 0));


        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

        launcherMotor = hardwareMap.get(DcMotorEx.class, "shooter_drive");
        // Optional: If the LauncherMotor runs backwards, uncomment the next line
        launcherMotor.setDirection(DcMotor.Direction.REVERSE);

        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        feederMotor = hardwareMap.get(DcMotor.class, "feeder_motor");

        liftServo = hardwareMap.get(Servo.class, "lift_servo");


        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        pinpoint.resetPosAndIMU();

        waitForStart();

        pinpoint.resetPosAndIMU();

        if (isStopRequested()) return;

        boolean moved = false;

        leftFrontDrive.setPower(-0.5);
        rightFrontDrive.setPower(-0.5);
        leftBackDrive.setPower(-0.5);
        rightBackDrive.setPower(-0.5);
        ElapsedTime timer2 = new ElapsedTime(ElapsedTime.SECOND_IN_NANO);
        timer2.reset();

        while(opModeIsActive()){

            while(!moved){
                if (pinpoint.getPosX(INCH) < -35) {
                    leftFrontDrive.setPower(0);
                    rightFrontDrive.setPower(0);
                    leftBackDrive.setPower(0);
                    rightBackDrive.setPower(0);
                    moved = true;
                } else {
                    pinpoint.update();
                }
            }
            if(timer2.seconds() <= 20) {
                switch (launchState) {
                    case IDLE:
                        if (feederTimer.seconds() > 2.5) {
                            // In the STEADY state, the LauncherMotor power does not change automatically.
                            // It holds its last value. We can still reset the timer to be ready.
                            launchState = ShooterWithIntake.LaunchStateEnum.SPIN_UP;
                        }
                        intakeMotor.setPower(-0.8);
                        feederMotor.setPower(-0.8);
                        liftServo.setPosition(INTAKE_POSITION);
                        break;
                    case SPIN_UP:
                        launcherMotor.setVelocity(LAUNCHER_TARGET_VELOCITY - 400);
                        if (launcherMotor.getVelocity() > LAUNCHER_MIN_VELOCITY - 400) {
                            launchState = ShooterWithIntake.LaunchStateEnum.LAUNCH;
                        }
                        intakeMotor.setPower(0);
                        feederMotor.setPower(0);
                        break;

                    case LAUNCH:
                        liftServo.setPosition(0.4);
                        feederTimer.reset();
                        launchState = ShooterWithIntake.LaunchStateEnum.LAUNCHING;
                        break;

                    case LAUNCHING:
                        if (feederTimer.seconds() > FEED_TIME_SECONDS) {

                            // lift servo down
                            liftServo.setPosition(LAUNCH_POSITION);
                            if (feederTimer.seconds() > LIFT_TIME_SECONDS) {
                                launchState = ShooterWithIntake.LaunchStateEnum.IDLE;
                                feederTimer.reset();
                            }
                        }
                }
            } else {
                leftFrontDrive.setPower(0.5);
                rightFrontDrive.setPower(-0.5);
                leftBackDrive.setPower(-0.5);
                rightBackDrive.setPower(0.5);
            }

            if(timer2.seconds() > 21.25) {
                requestOpModeStop();
            }

        }
    }
}
