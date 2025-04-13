import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.schedulers.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Calendar;
import java.util.LinkedList;

public class MainSimulation {

    public static void main(String[] args) {
        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            Datacenter datacenter = createDatacenter("Datacenter_0");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Create VMs
            List<Vm> vmList = new ArrayList<>();
            int vmCount = 5;
            for (int i = 0; i < vmCount; i++) {
                int mips = 1000;
                long size = 10000;
                int ram = 2048;
                long bw = 1000;
                int pesNumber = 1;
                String vmm = "Xen";

                Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                        new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }
            broker.submitVmList(vmList);

            // Create Cloudlets representing healthcare tasks
            List<Cloudlet> cloudletList = new ArrayList<>();
            cloudletList.addAll(createHealthcareCloudlets(brokerId));

            broker.submitCloudletList(cloudletList);

            // Load balancing: Assign cloudlets to VMs based on round-robin
            for (int i = 0; i < cloudletList.size(); i++) {
                Cloudlet cloudlet = cloudletList.get(i);
                Vm vm = vmList.get(i % vmList.size());
                broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
            }

            // Simulate VM failure (Fault Tolerance)
            simulateVmFailure(vmList, cloudletList, broker);

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletResults(newList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();
        int hostId = 0;

        int ram = 8192;
        long storage = 1000000;
        int bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000)));

        Host host = new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, new LinkedList<>());
    }

    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("HealthcareBroker");
    }

    private static List<Cloudlet> createHealthcareCloudlets(int userId) {
        List<Cloudlet> list = new ArrayList<>();
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        // Diagnosis task
        list.add(new Cloudlet(0, 5000, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
        list.get(0).setUserId(userId);

        // Report generation
        list.add(new Cloudlet(1, 3000, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
        list.get(1).setUserId(userId);

        // Alert
        list.add(new Cloudlet(2, 1000, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
        list.get(2).setUserId(userId);

        // Adding more diverse cloudlets
        list.add(new Cloudlet(3, 2000, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
        list.get(3).setUserId(userId);

        list.add(new Cloudlet(4, 4000, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
        list.get(4).setUserId(userId);

        return list;
    }

    private static void simulateVmFailure(List<Vm> vmList, List<Cloudlet> cloudletList, DatacenterBroker broker) {
        // Randomly fail one VM and reassign cloudlets
        Random rand = new Random();
        int failedVmIndex = rand.nextInt(vmList.size());
        Vm failedVm = vmList.get(failedVmIndex);
        System.out.println("\n⚠️ VM with ID " + failedVm.getId() + " is simulated as FAILED.\n");

        for (Cloudlet cl : cloudletList) {
            if (cl.getVmId() == failedVm.getId()) {
                // Reassign to another VM
                for (Vm vm : vmList) {
                    if (vm.getId() != failedVm.getId()) {
                        broker.bindCloudletToVm(cl.getCloudletId(), vm.getId());
                        System.out.println("Cloudlet " + cl.getCloudletId() + " reassigned to VM " + vm.getId());
                        break;
                    }
                }
            }
        }
    }

    private static void printCloudletResults(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== HEALTHCARE CLOUDLET EXECUTION RESULTS ==========");
        System.out.println("CloudletID" + indent + "STATUS" + indent +
                "DataCenterID" + indent + "VMID" + indent + "Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.println("SUCCESS" + indent +
                        cloudlet.getResourceId() + indent + indent +
                        cloudlet.getVmId() + indent + cloudlet.getActualCPUTime());
            }
        }
    }
}
