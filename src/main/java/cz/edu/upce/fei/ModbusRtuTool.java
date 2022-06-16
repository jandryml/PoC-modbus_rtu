package cz.edu.upce.fei;

import de.re.easymodbus.modbusclient.ModbusClient;
import gnu.io.CommPortIdentifier;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Command(name = "modbusRtuTool", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Connects and write to Modbus register/coil via Rtu.")
public class ModbusRtuTool implements Callable<Integer> {

    private Logger logger = Logger.getLogger(ModbusRtuTool.class.getName());

    @Option(names = {"-p", "--port"}, description = "Name of serial port. Auto-search if not filled.", defaultValue = "")
    private String comPort;

    @Option(names = {"-a", "--address"}, required = true, description = "Numeric address of the target register/coil")
    private int address;

    @Option(names = {"-t", "--type"}, required = true, description = "Type of input: {${COMPLETION-CANDIDATES}}")
    private InputType inputType;

    @Option(names = {"-u", "--unit"}, required = true, description = "Unit (slave) id.", defaultValue = "1")
    private byte unitId;

    @Parameters(index = "0", description = "Value that will be written to the target register/coil")
    private int value;

    public static void main(String[] args) {
        waitForUserInput();
        int exitCode = new CommandLine(new ModbusRtuTool()).execute(args);
        System.exit(exitCode);
    }

    private static void waitForUserInput() {
        System.out.println("Press any key to continue");
        new Scanner(System.in).nextLine();
    }

    @Override
    public Integer call() throws Exception {
        turnOffOutStream();
        if (comPort.isEmpty()) {
            logger.info("Port not defined. Running autodetect.");
            sendToAutodetectedPorts();
            return 1;
        } else {
            return sendData(comPort);
        }
    }

    /**
     * Send data to all detected serial ports.
     */
    private void sendToAutodetectedPorts() {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

            // sends only if the port is either RS485 or Serial
            if (CommPortIdentifier.PORT_RS485 == port.getPortType()
                    || CommPortIdentifier.PORT_SERIAL == port.getPortType()) {
                sendData(port.getName());
            }
        }
    }

    private int sendData(String comPort) {
        ModbusClient modbusClient = new ModbusClient();
        try {
            modbusClient.setUnitIdentifier(unitId);
            modbusClient.Connect(comPort);

            logger.info("Sending data to: " + comPort);

            switch (inputType) {
                case COIL: {
                    modbusClient.WriteSingleCoil(address, value != 0);
                    break;
                }
                case REGISTER: {
                    modbusClient.WriteSingleRegister(address, value);
                    break;
                }
            }
            logger.info("On " + comPort + ": Success sending.");
            modbusClient.Disconnect();
            return 0;
        } catch (Exception e) {
            logger.info("Communication error on " + comPort + ": " + e.getMessage());
            e.printStackTrace();
            return 1;
        } finally {
            try {
                modbusClient.Disconnect();
            } catch (IOException e) {
                logger.info("Error while disconnecting on " + comPort + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void turnOffOutStream() {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setOut(dummyStream);
    }
}
