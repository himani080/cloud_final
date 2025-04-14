import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import java.util.ArrayList;
import java.util.List;

public class HealthcareSimulation {

    public static void main(String[] args) {
        // Initialize the CloudSim framework
        try {
            CloudSim.init(1, null, false);  // 1 Datacenter Broker, no need for a cloudlet to start
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a broker for healthcare simulation tasks
        Broker broker = new Broker("HealthBroker");

        // Define healthcare-related VMs (representing medical servers)
        Vm medicalVm1 = new Vm(0, broker.getId(), 1000, 2, 2048, 10000, 10000, "Xen", new CloudletSchedulerTimeShared());
        Vm medicalVm2 = new Vm(1, broker.getId(), 1000, 2, 2048, 10000, 10000, "Xen", new CloudletSchedulerTimeShared());
        broker.submitVmList(List.of(medicalVm1, medicalVm2));

        // Create healthcare-related Cloudlets (tasks like processing medical data)
        Cloudlet medicalCloudlet1 = createMedicalCloudlet(1, broker.getId());
        Cloudlet medicalCloudlet2 = createMedicalCloudlet(2, broker.getId());
        Cloudlet medicalCloudlet3 = createMedicalCloudlet(3, broker.getId());

        // Add the tasks to the broker's cloudlet list
        broker.submitCloudletList(List.of(medicalCloudlet1, medicalCloudlet2, medicalCloudlet3));

        // Simulate scheduling and execution of healthcare tasks (e.g., MRI analysis)
        ExecutionScheduler executionSchedule = new ExecutionScheduler();
        executionSchedule.add(new CloudletExecutionInfo(medicalCloudlet1, 0, 1));  // Task 1 at time 0
        executionSchedule.add(new CloudletExecutionInfo(medicalCloudlet2, 1, 2));  // Task 2 at time 1
        executionSchedule.add(new CloudletExecutionInfo(medicalCloudlet3, 2, 3));  // Task 3 at time 2

        // Run the simulation and display results
        try {
            CloudSim.startSimulation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reporting Healthcare Simulation Status
        System.out.println("Healthcare System Simulation Started");
        System.out.println("Processing Tasks: " + executionSchedule.size());
for (CloudletExecutionInfo executionInfo : executionSchedule.getExecutionInfoList()) {
    System.out.println("Executing task: " + executionInfo.getCloudlet().getId());
    System.out.println("Scheduled at time: " + executionInfo.getStartTime() + ", Time required: " + executionInfo.getExecutionTime());
}


        CloudSim.stopSimulation();
    }

    private static Cloudlet createMedicalCloudlet(int id, int brokerId) {
        // Healthcare cloudlet representing tasks like MRI analysis or patient data processing
        Cloudlet cloudlet = new Cloudlet(id, 1000, brokerId, new CloudletSchedulerTimeShared());
        // Assigning additional healthcare-related attributes (e.g., patient data or medical image)
        cloudlet.setPatientData("Patient_ID_" + id);
        cloudlet.setTestName("Test_" + id);
        return cloudlet;
    }

    // Healthcare-specific Cloudlet Execution Info Class
    static class CloudletExecutionInfo {
        private Cloudlet cloudlet;
        private int startTime;
        private int executionTime;

        public CloudletExecutionInfo(Cloudlet cloudlet, int startTime, int executionTime) {
            this.cloudlet = cloudlet;
            this.startTime = startTime;
            this.executionTime = executionTime;
        }

        public Cloudlet getCloudlet() {
            return cloudlet;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getExecutionTime() {
            return executionTime;
        }
    }

    // Simple scheduler class to manage cloudlet executions
    static class ExecutionScheduler {
        private List<CloudletExecutionInfo> executionInfoList;

        public ExecutionScheduler() {
            executionInfoList = new ArrayList<>();
        }

        public void add(CloudletExecutionInfo info) {
            executionInfoList.add(info);
        }

        public int size() {
            return executionInfoList.size();
        }

        public List<CloudletExecutionInfo> getExecutionInfoList() {
            return executionInfoList;
        }
    }

    // Simulated Broker class for Healthcare tasks
    static class Broker {
        private String name;
        private int id;
        private List<Vm> vmList;
        private List<Cloudlet> cloudletList;

        public Broker(String name) {
            this.name = name;
            this.id = 0;  // Assume a broker ID for simplicity
            this.vmList = new ArrayList<>();
            this.cloudletList = new ArrayList<>();
        }

        public void submitVmList(List<Vm> vmList) {
            this.vmList.addAll(vmList);
        }

        public void submitCloudletList(List<Cloudlet> cloudletList) {
            this.cloudletList.addAll(cloudletList);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // Simulated Cloudlet class representing healthcare tasks (e.g., patient data processing)
    static class Cloudlet {
        private int id;
        private long length;
        private int brokerId;
        private CloudletScheduler scheduler;
        private String patientData;
        private String testName;

        public Cloudlet(int id, long length, int brokerId, CloudletScheduler scheduler) {
            this.id = id;
            this.length = length;
            this.brokerId = brokerId;
            this.scheduler = scheduler;
        }

        public void setPatientData(String patientData) {
            this.patientData = patientData;
        }

        public String getPatientData() {
            return patientData;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getTestName() {
            return testName;
        }

        public int getId() {
            return id;
        }
    }

    // Simulated VM class representing healthcare computing resources (e.g., servers for medical tasks)
    static class Vm {
        private int id;
        private int brokerId;
        private int mips;
        private int pes;
        private int ram;
        private long bw;
        private long size;
        private String hypervisor;
        private CloudletScheduler scheduler;

        public Vm(int id, int brokerId, int mips, int pes, int ram, long bw, long size, String hypervisor, CloudletScheduler scheduler) {
            this.id = id;
            this.brokerId = brokerId;
            this.mips = mips;
            this.pes = pes;
            this.ram = ram;
            this.bw = bw;
            this.size = size;
            this.hypervisor = hypervisor;
            this.scheduler = scheduler;
        }
    }
}
