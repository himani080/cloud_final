import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterFactory;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.schedulers.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

public class SimpleCloudSimExample {

    public static void main(String[] args) {
        try {
            // Initialize the CloudSim library
            CloudSim.init(1, null, false);

            // Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_1");

            // Create DatacenterBroker
            DatacenterBroker broker = createBroker();

            // Create Virtual Machines (VMs)
            List<Vm> vmList = createVMs(broker.getId());

            // Create Cloudlets (tasks)
            List<Cloudlet> cloudletList = createCloudlets(broker.getId());

            // Submit VMs and Cloudlets to the Broker
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Start the simulation
            CloudSim.startSimulation();

            // Retrieve and print the simulation results
            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
            printCloudletResults(finishedCloudlets);

            // Stop the simulation
            CloudSim.stopSimulation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create Datacenter
    private static Datacenter createDatacenter(String name) throws Exception {
        // Create a list to store host machines
        List<Host> hostList = new ArrayList<>();

        // Create Hosts (each host represents a machine)
        Host host = new Host(0, new RamProvisionerSimple(2048), new BwProvisionerSimple(10000),
                1000000, new ArrayList<Pe>(), new VmSchedulerTimeShared(new ArrayList<Pe>()));
        hostList.add(host);

        // Create Datacenter Characteristics (simple for this example)
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics("x86", "Linux", "Xen",
                hostList, 10000, 1000, 1000);

        // Create Datacenter
        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new ArrayList<>(), new LinkedList<>());
    }

    // Method to create DatacenterBroker
    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("Broker_1");
    }

    // Method to create Virtual Machines (VMs)
    private static List<Vm> createVMs(int brokerId) {
        List<Vm> vmList = new ArrayList<>();

        // Create a VM (simple one with MIPS, RAM, etc.)
        Vm vm = new Vm(0, brokerId, 1000, 1, 1024, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
        vmList.add(vm);

        return vmList;
    }

    // Method to create Cloudlets (tasks)
    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Create Cloudlets (simple tasks with some resource usage)
        UtilizationModel utilizationModel = new UtilizationModelFull();
        Cloudlet cloudlet = new Cloudlet(0, 1000, 1, 500, 1000, utilizationModel, utilizationModel, utilizationModel);
        cloudlet.setUserId(brokerId);
        cloudletList.add(cloudlet);

        return cloudletList;
    }

    // Method to print Cloudlet results
    private static void printCloudletResults(List<Cloudlet> cloudlets) {
        for (Cloudlet cloudlet : cloudlets) {
            System.out.println("Cloudlet ID: " + cloudlet.getCloudletId() + " - Status: " + cloudlet.getStatusString());
        }
    }
}
