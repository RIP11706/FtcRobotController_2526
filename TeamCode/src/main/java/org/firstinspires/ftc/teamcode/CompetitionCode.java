package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp
public class CompetitionCode extends OpMode {
    private DcMotor launcherMotor;
    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;
    public final double launcherClosePower = .5;
    @Override
    public void init() {
        launcherMotor = hardwareMap.get(DcMotor.class, "shooter_drive");
        launcherMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeftMotor = hardwareMap.get(DcMotor.class, "front_left");
        backLeftMotor = hardwareMap.get(DcMotor.class, "back_left");
        frontRightMotor = hardwareMap.get(DcMotor.class, "front_right");
        backRightMotor = hardwareMap.get(DcMotor.class, "back_right");
    }

    @Override
    public void loop() {
        if(gamepad1.x){
            launcherMotor.setPower(launcherClosePower);
        } else {
            launcherMotor.setPower(0);
        }

        double forwards = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
        double strafing =  gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
        double turning = gamepad1.right_stick_x;

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(forwards) + Math.abs(strafing) + Math.abs(turning), 1);
        double frontLeftPower = (forwards + strafing + turning) / denominator;
        double backLeftPower = (forwards - strafing + turning) / denominator;
        double frontRightPower = (forwards - strafing - turning) / denominator;
        double backRightPower = (forwards + strafing - turning) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);
    }
}
