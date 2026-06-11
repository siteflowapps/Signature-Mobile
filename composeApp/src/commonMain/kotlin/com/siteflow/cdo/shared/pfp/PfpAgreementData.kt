package com.siteflow.cdo.shared.pfp

data class AgreementClause(
    val id: Int,
    val title: String,
    val text: String? = null,
    val subClauses: List<Pair<String, String>> = emptyList()
)

val PFP_CLAUSES = listOf(
    AgreementClause(1, "Program Facilitator",
        "This Program will be facilitated by Silveraxis Technologies Private Limited, on behalf of Reliance Consumer Products Limited (\"RCPL\")."),
    AgreementClause(2, "Eligibility",
        "This Program is open to all Indian Citizens, above the age of 18 years, residing in India only (\"Participants\"). Employees of RCPL and its affiliates, subsidiaries, assignees, representatives and their immediate families are not eligible to participate in the Program. Residents of other countries residing in India or non-resident Indians are not eligible to participate. "),
    AgreementClause(3, "Participation Process", subClauses = listOf(
        "3.i"   to "The Program is open only to Campa destination outlets engaged with RCPL in general trade (GT), including, but not limited to, Grocery Traditional, Grocery MT (OAG) outlets, Eating & Dining (E&D) outlets, Convenience store outlets, Highway Dhabas, Colleges, Paan shops, Bus stand and Bakery outlets (collectively referred to as the “Participating Outlets”). These Participating outlets will be known as Campa Destination Outlets.",
        "3.ii"  to "Participating Outlets must complete the onboarding requirements as prescribed and communicated by RCPL/Silveraxis Technologies Private Limited App, from time to time, including submission of all necessary documents, proof and other details",
        "3.iii" to "Participating Outlets, where a volume-based monthly payout as per monthly slab achievement, shall be eligible for a Payout on a monthly basis, subject to a classification slab (\"Eligible Outlets\"). For the purpose of the Payout to Eligible Outlets, classification shall be as set out under Annexure 1.",
        "3.iv"  to "Each Participating Outlet shall maintain a minimum warm-stock display as per classifications provided under Annexure 2. ",
        "3.v"   to "ASE, ASM followed by Commercial Finance shall validate the eligibility of the Participating Outlet by auditing and reviewing for the relevant period, sales volumes, invoices, sales/order related documents, including photographs of stock/key visual/Asset placement. The Participating Outlet agrees to participate and undertakes to provide true information and full co-operation in any audit conducted as part of and during the term of this Program."
    )),
    AgreementClause(4, "Payout Conditions", subClauses = listOf(
        "4.i"     to "Payout shall be provided only to those eligible Outlets that comply with the following among other conditions that are informed from time to time: ",
        "4.i.i"   to "The Eligible Outlets shall achieve the monthly sales volumes as per slabs to a qualify slab wise payout, ",
        "4.i.ii"  to "The Eligible Outlets must upload the invoices in due time for monthly payout as per volume slab achievement.",
        "4.i.iii" to "The Eligible Outlets shall maintain the required warm-stock display for their classification, (Subject to agreement between sales team and retailer.)",
        "4.ii"    to "Payouts, where applicable, shall be processed on a monthly basis, as per the calendar month, and subject to successful audit, review and validation by the Sales team ( ASE, ASM), Commercial Finance Team and Silveraxis Technologies Private Limited",
        "4.iii"   to "Payouts are non-transferable and shall be provided in such a manner, timing, and mode as determined by RCPL at its sole discretion. ",
        "4.iv"    to "RCPL reserves the right to withhold or recover the Payouts in case of non-compliance, misrepresentation, fraud, and/or failure to meet verification/audit requirements. ",
    )),
    AgreementClause(5, "Verification",
        "Upon selecting the Eligible Outlets as stated in clause 4, the Silveraxis Technologies Private Limited shall contact the Eligible Outlets for verification. To claim the Payouts, the Participants must submit/show all relevant details/documents for review in the manner communicated by Silveraxis Technologies Private Limited in the app. The Participants agree for use of the information shared by them under this Program for among other purposes, identification and/or verification of Eligible Outlets and their details and for any other purpose as stated in this document. ."),
    AgreementClause(6, "Failure to Provide Documents",
        "In case the Participants fails to furnish details/documents on or before the designated date as communicated, the Eligible Outlets shall be deemed to have withdrawn from the Program in the current month. "),
    AgreementClause(7, "Payment Processing",
        "Upon receipt and validation of information from the Participants, Silveraxis Technologies Private Limited shall initiate the process of transmission of Payouts. All payments shall be made in Indian Rupees (INR)."),
    AgreementClause(8, "Acceptance of Terms",
        "Participation in this Program implies that the Participants agree to all Terms and Conditions of the Program and have made themselves aware of the said Terms and Conditions of the Program. "),
    AgreementClause(9, "Voluntary Participation",
        "Participation in this Program is voluntary. By participating in the Program, participants agree to receive communication(s) from RCPL and/or its Agencies relating to the Program and Participants unconditionally agrees not to make any claims or raise any complaints against RCPL and/or its Agencies. ."),
    AgreementClause(10, "Use of Information",
        "By participating in the Program, the Participant hereby consents to the accessing of its information and/or images by RCPL and/or the Silveraxis Technologies Private Limited for the purpose of the Program. Further, RCPL shall be at liberty to use the entries received for its media coverage, advertisement and publicity in any form which is existing today or will be known anytime in the future without any further consideration to the Participants including for the promotion of any products manufactured, distributed and/or supplied by RCPL. The Participant hereby consents to the use of their entries for the purpose stated herein in perpetuity and worldwide."),
    AgreementClause(11, "Right to Reject Participation",
        "RCPL reserves the right to reject any Participant’s entry or continued participation in the Program solely at its own discretion and without further notice or reasons. "),
    AgreementClause(12, "Program Modification",
        "RCPL reserves the right to cancel, modify, alter/amend, extend or withdraw the Program and its terms at any time at its sole and absolute discretion and without further notice."),
    AgreementClause(13, "Non-transferable Payout",
        "Payouts are non-transferable."),
    AgreementClause(14, "NDNC / DND Consent",
        "RCPL or the Silveraxis Technologies Private Limited will not be responsible for any NDNC (National Do Not Call) Registry regulation that will come into play. All Participants who participate will agree as per the terms and conditions that even if they are registered under NDNC, DND (Do Not Disturb), RCPL or the Silveraxis Technologies Private Limited will have all the authority to call such Participants by virtue of them having voluntarily participated in the Program."),
    AgreementClause(15, "Limitation of Liability",
        "Participants agree that RCPL shall not be liable for any claims, costs, injuries, losses or damages of any kind arising out of or in connection with the Program or with the acceptance, possession, or use of any Payouts."),
    AgreementClause(16, "Intellectual Property",
        "All rights, title and interest including but not limited to the Intellectual Property Rights in the promotional material(s) and any/all registrations received shall vest solely and exclusively with RCPL at all times and RCPL shall be entitled to use the database of the entries received or any information in any media for future promotional, marketing and publicity purpose without any further reference or payment or compensation to the participants."),
    AgreementClause(17, "Confidentiality",
        "All confidential information and intellectual property shared or accessed under this Program shall remain the exclusive property of RCPL and must not be disclosed, used, or reproduced at any given point in time, except as set out in this Terms and Conditions."),
    AgreementClause(18, "Copyright Responsibility",
        "Participant(s) shall be solely responsible for any consequences which may arise due to any kind of infringement of copyrights or any kind of intellectual property rights belonging to any other person/entity etc. and also undertake to fully indemnify RCPL and its officers, directors, employees etc. on the happening of such an event (including without limitation cost of attorney, legal charges, etc.). "),
    AgreementClause(19, "Indemnity",
        "Participants hereby agrees to indemnify and keep RCPL, its associated companies, its agencies and their respective directors, officers, employees, contractors and agents, indemnified against any and all losses, claims (including but not limited to third party claims), injuries, costs, fees, fines, penalties, taxes, charges and any other liability arising out of any act of omission, commission, fraud, negligence or misconduct by the Participants."),
    AgreementClause(20, "Governing Law",
        "All disputes shall be governed by the laws of India. The Program comes under the jurisdiction of the courts located in Mumbai.")
)

val VOLUME_SLAB_TABLE = listOf(
    Triple("Platinum", "300 cs & Above", "₹8/case"),
    Triple("Diamond",  "150 – 299 cs",   "₹6/case"),
    Triple("Gold",     "81 – 149 cs",    "₹4/case"),
    Triple("Silver",   "40 – 80 cs",     "₹3/case")
)

val WARM_STOCK_TABLE = listOf(
    "Silver"   to "30 cases",
    "Gold"     to "40 cases",
    "Diamond"  to "50 cases",
    "Platinum" to "60 cases"
)
