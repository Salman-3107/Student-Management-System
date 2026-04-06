package com.example.student_management_system;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AdmissionsController extends BasePageController {

    @FXML
    protected void showHome_section() {
        showInfo("Admissions Home", "You are viewing the UG Admission 2025-26 homepage.");
    }

    @FXML
    protected void showProspectus() {
        showInfo("Prospectus", "The BUET Undergraduate Prospectus 2025-26 contains detailed information about all programs, eligibility criteria, course structures, and campus facilities.\n\nDownload available at: buet.ac.bd/web/#/ugAdmission/1");
    }

    @FXML
    protected void showForeignInstructions() {
        showInfo("Instructions For Foreign Nationals",
                "Foreign nationals seeking admission to BUET must:\n\n" +
                "• Apply through the Bangladesh Government (Ministry of Education) or Embassy\n" +
                "• Submit equivalent SSC and HSC certificates with GPA verification\n" +
                "• Obtain a No-Objection Certificate (NOC) from the Ministry of Education\n" +
                "• Fulfill language proficiency requirements (English)\n\n" +
                "Contact: ugadmission@buet.ac.bd for detailed guidance.");
    }

    @FXML
    protected void showPaymentProcess() {
        showInfo("Payment Process",
                "Admission fee payment can be made through:\n\n" +
                "• Sonali Bank branches across Bangladesh\n" +
                "• Online Banking (selected banks)\n" +
                "• Mobile Banking: bKash, Nagad, Rocket\n\n" +
                "Payment Challan is generated after successful online application at the BUET admission portal.\n\nKeep the payment receipt for all future correspondence.");
    }

    @FXML
    protected void showFAQ() {
        showInfo("Frequently Asked Questions",
                "Q: Can I apply if I passed HSC from outside Bangladesh?\n" +
                "A: Yes, with equivalent certificates verified by the Ministry of Education.\n\n" +
                "Q: Is there a negative marking in the admission test?\n" +
                "A: Yes, 0.25 marks are deducted for each wrong answer.\n\n" +
                "Q: What subjects are tested in the admission exam?\n" +
                "A: Physics, Chemistry, Mathematics, and English.\n\n" +
                "Q: Can I get a hostel seat as a first-year student?\n" +
                "A: All freshers are given priority for hall allocation. Most first-year students receive a seat.");
    }

    @FXML
    protected void openDeptAlloc2Eng() {
        showInfo("Department Allocation (2nd Run) — Engineering & URP",
                "The 2nd Run Department Allocation for Engineering and URP candidates has been published on 08/03/2026.\n\n" +
                "Candidates must report to their allocated departments by the specified date with original certificates and documents.\n\n" +
                "For details, visit: buet.ac.bd/web/#/ugAdmission/1");
    }

    @FXML
    protected void openDeptAlloc2Arch() {
        showInfo("Department Allocation (2nd Run) — Architecture",
                "The 2nd Run Department Allocation for Architecture candidates has been published on 08/03/2026.\n\n" +
                "Architecture candidates must appear for the drawing test as part of the admission process.\n\n" +
                "Contact: Department of Architecture, BUET");
    }

    @FXML
    protected void openMedicalForm() {
        showInfo("Medical Screening Form",
                "All selected candidates must complete the Medical Screening Form before final enrollment.\n\n" +
                "The form must be filled by a registered MBBS doctor and submitted to the BUET Medical Centre.\n\n" +
                "Medical Centre Location: Within BUET campus, West Palashi\n" +
                "Office Hours: Sunday–Thursday, 9 AM – 3 PM");
    }

    @FXML
    protected void openDeptAlloc1Eng() {
        showInfo("Department Allocation (1st Run) — Engineering & URP",
                "The 1st Run Department Allocation for Engineering and URP candidates was published on 17/02/2026.\n\n" +
                "Candidates who were allocated in the 1st Run but did not confirm admission may have lost their seat.\n\n" +
                "For 2nd Run results, check the latest admission notice.");
    }

    @FXML
    protected void openDeptAlloc1Arch() {
        showInfo("Department Allocation (1st Run) — Architecture",
                "The 1st Run Department Allocation for Architecture candidates was published on 17/02/2026.\n\n" +
                "Contact the Department of Architecture for any queries regarding allocation.");
    }
}
