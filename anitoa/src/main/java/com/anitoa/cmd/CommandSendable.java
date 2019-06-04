package com.anitoa.cmd;

public interface CommandSendable {

     byte[] sendPcrCommandSync(PcrCommand command);
     int sendPcrCommand(PcrCommand command);

}
