package com.radiusnetworks.lib;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NTPClient
{
    private String serverHostName = null;
    private  List<String> listTime = new ArrayList<String>();

    public NTPClient()
    {
        serverHostName = "pool.ntp.org";
    }

    public NTPClient(String aHostAddress)
    {
        serverHostName = aHostAddress;
    }

    private void processResponse(TimeInfo info)
    {
        NtpV3Packet message = info.getMessage();

        TimeStamp refNtpTime = message.getReferenceTimeStamp();
//        System.out.println(" Reference Timestamp:\t" + refNtpTime + "  " + refNtpTime.toDateString());
        listTime.add(refNtpTime.toDateString());

        // Originate Time is time request sent by client (t1)
        TimeStamp origNtpTime = message.getOriginateTimeStamp();
//        System.out.println(" Originate Timestamp:\t" + origNtpTime + "  " + origNtpTime.toDateString());
        listTime.add(refNtpTime.toDateString());

        long destTime = info.getReturnTime();
        // Receive Time is time request received by server (t2)
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
//        System.out.println(" Receive Timestamp:\t" + rcvNtpTime + "  " + rcvNtpTime.toDateString());
        listTime.add(refNtpTime.toDateString());

        // Transmit time is time reply sent by server (t3)
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
//        System.out.println(" Transmit Timestamp:\t" + xmitNtpTime + "  " + xmitNtpTime.toDateString());
        listTime.add(refNtpTime.toDateString());

        // Destination time is time reply received by client (t4)
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
//        System.out.println(" Destination Timestamp:\t" + destNtpTime + "  " + destNtpTime.toDateString());
        listTime.add(refNtpTime.toDateString());
    }

    public String getTime(int i)
    {
        NTPUDPClient client = new NTPUDPClient();

        try
        {
            client.setDefaultTimeout(10000);
            client.open();

            listTime.clear();

            InetAddress hostAddr = InetAddress.getByName(serverHostName);
            TimeInfo info = client.getTime(hostAddr);

            processResponse(info);
            client.close();

            return listTime.get(i);
        }
        catch (SocketException e)
        {
            return null;
        }
        catch (UnknownHostException e)
        {
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
