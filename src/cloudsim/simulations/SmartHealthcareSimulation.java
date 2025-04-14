import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;

public class SmartHealthcareSimulation {

    public static void main(String[] args) {
        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            Datacenter datacenter = createDatacenter("Datacenter_1");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            List<Vm> vmList = new ArrayList<>();
            int vmCount = 5;

            // Create VMs
            for (int i = 0; i < vmCount; i++) {
                int mips = 1000;
                int ram = 512;
                long storage = 10000;
                int bw = 1000;
                int pesNumber = 1;

                Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, storage, "Xen", new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }

            broker.submitVmList(vmList);

            List<Cloudlet> cloudletList = new ArrayList<>();
            int cloudletCount = 10;

            // Create Cloudlets (medical tasks)
            for (int i = 0; i < cloudletCount; i++) {
                long length = 4000;
                int pesNumber = 1;
                long fileSize = 300;
                long outputSize = 300;

                UtilizationModel utilizationModel = new UtilizationModelFull();
                Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel,
                        utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
            }

            // Fault-tolerance: Randomly mark 1 VM as faulty
            int faultyVmIndex = new Random().nextInt(vmCount);
            Vm faultyVm = vmList.get(faultyVmIndex);
            System.out.println("⚠️ Simulating VM Failure on VM ID: " + faultyVm.getId());

            // Round-robin load balancing (excluding faulty VM)
            int vmIndex = 0;
            for (Cloudlet cloudlet : cloudletList) {
                while (vmIndex == faultyVmIndex) {
                    vmIndex = (vmIndex + 1) % vmCount;
                }
                cloudlet.setVmId(vmList.get(vmIndex).getId());
                vmIndex = (vmIndex + 1) % vmCount;
            }

            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(newList);
            System.out.println("\nSimulation finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();

        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000))); // a CPU core

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
                storage, peList, new VmSchedulerTimeShared(peList)));

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, new LinkedList<>());
    }

    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("Broker");
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== OUTPUT ==========\n");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.print("SUCCESS");

                System.out.println(indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() +
                        indent + cloudlet.getActualCPUTime() + indent + cloudlet.getExecStartTime() + indent + cloudlet.getFinishTime());
            }
        }
    }
}
