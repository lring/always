﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Agent.UI
{
    class ReetiTranslation
    {
        private ReetiCommunication reeti = new ReetiCommunication();

        private const String HORIZONTAL        = "horizontal";
        private const String VERTICAL          = "vertical";
        private const String headNod           = "Global.SmallNod.play();";
        private const String neckRotate        = "Global.servo.neckRotat =";
        private const String neckTilt          = "Global.servo.neckTilt =";
        private const String leftEyePan        = "Global.servo.leftEyePan =";
        private const String leftEyeTilt       = "Global.servo.leftEyeTilt =";
        private const String rightEyePan       = "Global.servo.rightEyePan =";
        private const String rightEyeTilt      = "Global.servo.rightEyeTilt =";
        private const String neutralPosition   = "Global.servo.neutralPosition();"; 
        private const String smileFace         = "Global.Happy.play();";
        private const String concernFace       = "Global.Sad.play();";
        private const String beginSpeech       = "Global.Talk.play(0.3);";
        private const String endSpeech         = "Global.Talk.play(0.3);";

        private String ConstructMessage(double HorOutput, double VerOutput)
        {
            String cmd;
            cmd  = neckRotate + HorOutput + " smooth:1s,";
            cmd += neckTilt   + VerOutput + " smooth:1s,";
            return cmd;
        }

        private double findOutput(String HorOrVer, String cmd)
        {
            double output;
            int start = 0, end = 0;

            if (HorOrVer.Equals(HORIZONTAL))
            {
                start = cmd.IndexOf(HORIZONTAL) + 12;
                end = cmd.IndexOf(VERTICAL) - 2;
            }
            else if (HorOrVer.Equals(VERTICAL))
            {
                if (cmd.Contains("bookmark"))
                {
                    start = cmd.IndexOf(VERTICAL) + 10;
                    end = cmd.IndexOf("</bookmark>") - 1;
                }
                else
                {
                    start = cmd.IndexOf(VERTICAL) + 10;
                    end = cmd.IndexOf("/>") - 2;
                }

            }
            output = Convert.ToSingle(cmd.Substring(start, end - start));
            return output;
        }

        private double mapOutput(String HorOrVer, double output)
        {
            if (HorOrVer.Equals(HORIZONTAL))
            {
                //Mirror the Agent 
                if (output > 0)
                {
                    //output *= -1;
                    output = (output * 25) + 50;
                }
                else if (output < 0)
                {
                    output = (output * 25);
                }
                else
                    output = 50;
            }
            else if (HorOrVer.Equals(VERTICAL))
            {
                if (output < 0)
                {
                    output *= -1;
                    output = (output * 25);
                }
                else if (output > 0)
                {
                    output = (output * 25) + 60;
                }
                else
                    output = 55.56;
            }
            return output;
        }

        public void TranslateToReetiCommand(String task, String Command)
        {
            if (Command.Contains("NOD") && task.Equals("perform"))
            {
                SendCommand(headNod);
            }
            //TODO: check if face tracking!!!
            if (Command.Contains("GAZE")) //&& task.Equals("speech") )
            {
                double HorOutput = mapOutput(HORIZONTAL, findOutput(HORIZONTAL, Command));
                double VerOutput = mapOutput(VERTICAL, findOutput(VERTICAL, Command));

                String command = ConstructMessage(HorOutput, VerOutput);

                if(HorOutput > 50) {
                    command += rightEyePan + "80 smooth:0.5s, " + rightEyeTilt + "60 smooth:0.5s, ";
                    command += leftEyePan + "60 smooth:0.5s, " + leftEyeTilt + "60 smooth:0.5s;";
                }
                else {
                    command += rightEyePan + "60 smooth:0.5s, " + rightEyeTilt + "40 smooth:0.5s, ";
                    command += leftEyePan + "40 smooth:0.5s, " + leftEyeTilt + "42.55 smooth:0.5s;";
                }

                SendCommand(command);
            }
            if (Command.Contains("CONCERN"))
            {
                SendCommand(concernFace);
            }
            if (Command.Contains("SMILE"))
            {
                SendCommand(smileFace);
            }
            if (Command.Contains("WARM"))
            {
                SendCommand(neutralPosition);
            }
            if (Command.Contains("BEGINSPEECH"))
            {
                SendCommand(beginSpeech);
            }
            if (Command.Contains("ENDSPEECH"))
            {
                SendCommand(endSpeech);
            }
        }

        private void SendCommand(String Command)
        {
            reeti.Send(Command);
        }
    }
}
