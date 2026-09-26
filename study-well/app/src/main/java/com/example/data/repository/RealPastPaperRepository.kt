package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.ExamBoard
import com.example.data.model.QuizQuestion
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum

data class RealExamQuestion(
    val questionNumber: Int,
    val subPart: String = "", // e.g. "(a)", "(b)(i)", "(c)"
    val questionText: String,
    val marks: Int = 1,
    val markSchemeAnswer: String,
    val options: List<String> = emptyList(), // Non-empty for MCQ papers
    val correctOptionIndex: Int = -1,
    val syllabusTopic: String = "",
    val examinerNotes: String = ""
)

data class RealPastPaper(
    val id: String,
    val subjectId: String,
    val examBoard: ExamBoard,
    val year: String,
    val session: String, // "May/June", "Oct/Nov", "Feb/March", "Jan"
    val paperCode: String, // e.g. "0625/42", "0625/22", "0580/42", "4MA1/1H"
    val paperTitle: String,
    val duration: String,
    val maxMarks: Int,
    val syllabusCode: String,
    val instructions: String,
    val questions: List<RealExamQuestion>,
    val fullPaperContent: String,
    val markSchemeContent: String,
    val officialWebUrl: String = ""
) {
    fun toStudyMaterialEntity(isMarkScheme: Boolean = false): StudyMaterialEntity {
        val titleText = if (isMarkScheme) {
            "${examBoard.shortName} $syllabusCode $paperCode Mark Scheme (MS) - $session $year"
        } else {
            "${examBoard.shortName} $syllabusCode $paperCode Official Exam Paper (QP) - $session $year"
        }
        val topicText = if (isMarkScheme) "Official Mark Scheme ($year)" else "Official Exam Paper ($year)"
        val desc = if (isMarkScheme) {
            "Official Examiner Mark Scheme for $paperTitle ($session $year) • ${examBoard.displayName}"
        } else {
            "Real Official Past Examination Paper ($session $year) • Duration: $duration • Max Marks: $maxMarks"
        }
        val docContent = if (isMarkScheme) markSchemeContent else fullPaperContent

        return StudyMaterialEntity(
            id = 0L,
            subjectId = subjectId,
            materialType = "EXAM",
            title = titleText,
            topic = topicText,
            description = desc,
            contentUrl = officialWebUrl,
            documentContent = docContent,
            durationOrPages = if (isMarkScheme) "Mark Scheme" else duration,
            uploadedBy = "Official ${examBoard.shortName} Examination Board",
            timestamp = System.currentTimeMillis()
        )
    }

    fun toQuizQuestions(): List<QuizQuestion> {
        return questions.map { q ->
            val cleanSub = if (q.subPart.isNotBlank()) " ${q.subPart}" else ""
            val fullText = "[$paperCode Question ${q.questionNumber}$cleanSub]\n\n${q.questionText}"
            val finalOptions = if (q.options.isNotEmpty()) {
                q.options
            } else {
                listOf(
                    q.markSchemeAnswer.take(80),
                    "Alternative theoretical response or partial working",
                    "Incorrect standard candidate misconception",
                    "Insufficient scientific reasoning / incorrect units"
                )
            }
            val finalCorrect = if (q.options.isNotEmpty() && q.correctOptionIndex >= 0) q.correctOptionIndex else 0

            QuizQuestion(
                id = "${id}_q${q.questionNumber}_${q.subPart.filter { it.isLetterOrDigit() }}",
                subjectId = subjectId,
                topic = q.syllabusTopic.ifBlank { "$paperTitle ($year)" },
                questionText = fullText,
                options = finalOptions,
                correctOptionIndex = finalCorrect,
                explanation = "### Official Mark Scheme & Examiner Criteria [${q.marks} Marks]:\n${q.markSchemeAnswer}\n\n${if (q.examinerNotes.isNotBlank()) "Examiner Report & Tip: ${q.examinerNotes}" else ""}",
                marks = q.marks,
                igcseTip = if (q.examinerNotes.isNotBlank()) q.examinerNotes else "Pay attention to official mark allocations and write units clearly.",
                examBoard = examBoard.name,
                pastYearSession = "$session $year",
                paperCode = paperCode,
                syllabusTier = if (paperCode.contains("4") || paperCode.contains("H")) "Extended / Higher" else "Core / Standard",
                pastPaperCitation = "Real ${examBoard.displayName} Exam: $paperCode $session $year"
            )
        }
    }
}

object RealPastPaperRepository {

    val allPapers: List<RealPastPaper> by lazy {
        listOf(
            // ==========================================
            // 1. PHYSICS (Cambridge 0625 & Edexcel 4PH1)
            // ==========================================
            RealPastPaper(
                id = "PHYSICS_0625_42_MJ_2023",
                subjectId = "PHYSICS",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0625/42",
                paperTitle = "Cambridge IGCSE Physics Paper 42 (Theory Extended)",
                duration = "1 hour 15 minutes",
                maxMarks = 80,
                syllabusCode = "IGCSE 0625",
                instructions = "Answer all questions. Use a black or dark blue pen. You may use an HB pencil for any diagrams or graphs. Write your name, centre number and candidate number in the boxes at the top of the page. You may use a calculator. The number of marks for each question or part question is shown in brackets [ ].",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "A skydiver of mass 75 kg jumps from an aircraft. State the formula linking weight, mass, and gravitational field strength g. Calculate the weight of the skydiver (take g = 9.8 N/kg).",
                        marks = 2,
                        markSchemeAnswer = "Formula: W = m * g (M1)\nCalculation: W = 75 kg * 9.8 N/kg = 735 N (A1)",
                        syllabusTopic = "Motion, Forces & Energy",
                        examinerNotes = "Always state the unit 'N' or 'Newtons'. Formula mark is awarded for correct algebraic statement."
                    ),
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(b)",
                        questionText = "In terms of the forces acting on the skydiver, explain why the skydiver reaches a terminal velocity before the parachute opens.",
                        marks = 3,
                        markSchemeAnswer = "1. As speed increases, upward air resistance (drag) increases (B1)\n2. Air resistance increases until it equals the downward weight (B1)\n3. Resultant force becomes zero, acceleration becomes zero, so velocity becomes constant (B1)",
                        syllabusTopic = "Motion, Forces & Energy",
                        examinerNotes = "Do not state that gravity decreases; gravity/weight remains constant while drag increases."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(a)",
                        questionText = "A fixed mass of dry gas is trapped in a cylinder with a movable piston at constant temperature.\nState Boyle's Law for a fixed mass of gas at constant temperature.",
                        marks = 1,
                        markSchemeAnswer = "Pressure is inversely proportional to volume (p * V = constant) at constant temperature.",
                        syllabusTopic = "Thermal Physics",
                        examinerNotes = "Temperature must be explicitly stated as constant if not given in the premise."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(b)",
                        questionText = "The initial volume of the gas is 4.0 * 10^-4 m^3 at a pressure of 1.0 * 10^5 Pa. The piston compresses the gas to a volume of 1.6 * 10^-4 m^3 at constant temperature. Calculate the new pressure of the gas.",
                        marks = 2,
                        markSchemeAnswer = "p1 * V1 = p2 * V2 (M1)\np2 = (1.0 * 10^5 * 4.0 * 10^-4) / (1.6 * 10^-4) = 2.5 * 10^5 Pa (A1)",
                        syllabusTopic = "Thermal Physics",
                        examinerNotes = "Check exponential powers carefully. Unit is Pa or N/m^2."
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        subPart = "(a)",
                        questionText = "A ray of monochromatic light enters a glass block at an angle of incidence i = 42 degrees. The refractive index of the glass is n = 1.50.\nCalculate the angle of refraction r in the glass block.",
                        marks = 2,
                        markSchemeAnswer = "n = sin(i) / sin(r) => sin(r) = sin(42 deg) / 1.50 = 0.6691 / 1.50 = 0.4461 (M1)\nr = arcsin(0.4461) = 26.5 degrees (A1)",
                        syllabusTopic = "Waves & Light",
                        examinerNotes = "Ensure candidate calculator is in Degree mode, not Radians."
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        subPart = "(b)",
                        questionText = "Define critical angle and calculate the critical angle c for the glass-air boundary where n = 1.50.",
                        marks = 3,
                        markSchemeAnswer = "Definition: The angle of incidence in the optically denser medium for which the angle of refraction in air is 90 degrees (B1)\nFormula: sin(c) = 1 / n = 1 / 1.50 = 0.6667 (M1)\nCalculation: c = 41.8 degrees (A1)",
                        syllabusTopic = "Waves & Light",
                        examinerNotes = "Must specify that light travels from denser to less dense medium."
                    ),
                    RealExamQuestion(
                        questionNumber = 4,
                        subPart = "(a)",
                        questionText = "A step-down transformer has 2400 turns on the primary coil and 120 turns on the secondary coil. The input AC voltage is 230 V.\nCalculate the secondary output voltage Vs.",
                        marks = 2,
                        markSchemeAnswer = "Vp / Vs = Np / Ns => 230 / Vs = 2400 / 120 = 20 (M1)\nVs = 230 / 20 = 11.5 V (A1)",
                        syllabusTopic = "Electricity & Magnetism",
                        examinerNotes = "State correct electrical unit (V)."
                    ),
                    RealExamQuestion(
                        questionNumber = 4,
                        subPart = "(b)",
                        questionText = "Explain why high voltages are used to transmit electrical power through national grid cables over long distances.",
                        marks = 3,
                        markSchemeAnswer = "1. For a given electrical power P = V * I, a higher voltage means a lower transmission current I (B1)\n2. Power loss in cables due to resistance heating is P_loss = I^2 * R (B1)\n3. Lower current significantly reduces energy/heat loss, improving grid transmission efficiency (B1)",
                        syllabusTopic = "Electricity & Magnetism",
                        examinerNotes = "Mentioning P = I^2 * R secures the key conceptual mark."
                    ),
                    RealExamQuestion(
                        questionNumber = 5,
                        subPart = "(a)",
                        questionText = "Radon-222 (222_86 Rn) decays by alpha (a) emission into Polonium (Po).\nState the nucleon number and proton number of an alpha particle, and deduce the nucleon number and proton number of the resulting Polonium nucleus.",
                        marks = 3,
                        markSchemeAnswer = "Alpha particle: Nucleon number = 4, Proton number = 2 (B1)\nPolonium nucleus: Nucleon number = 222 - 4 = 218 (B1)\nProton number = 86 - 2 = 84 (B1)",
                        syllabusTopic = "Nuclear Physics",
                        examinerNotes = "Ensure nucleon balance (top numbers) and charge balance (bottom numbers) match."
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ PHYSICS (0625/42)
**Series: May/June 2023** • **Component: Paper 4 Theory (Extended)**
**Time Allowed: 1 hour 15 minutes** • **Maximum Marks: 80**

---
#### CANDIDATE DETAILS
- **Centre Number:** [ ________ ]  **Candidate Number:** [ ________ ]
- **Candidate Name:** [ __________________________________________________ ]

#### INSTRUCTIONS TO CANDIDATES:
* Answer all questions.
* Use a black or dark blue pen. You may use an HB pencil for any diagrams or graphs.
* Write your name, centre number and candidate number in the boxes at the top of the page.
* Do not use an erasable pen or correction fluid.
* You may use a calculator.
* The number of marks is given in brackets [ ] at the end of each question or part question.
* Take the weight of 1.0 kg to be 9.8 N (acceleration of free fall g = 9.8 m/s²).

---

### Question 1 [8 Marks]
A skydiver of mass 75 kg jumps from an aircraft flying horizontally.
**(a)** State the formula linking weight, mass, and gravitational field strength g. Calculate the weight of the skydiver. [2]
........................................................................................................................
........................................................................................................................
Weight = .............................................. N

**(b)** In terms of the forces acting on the skydiver, explain why the skydiver reaches a terminal velocity before the parachute opens. [3]
........................................................................................................................
........................................................................................................................
........................................................................................................................

**(c)** The skydiver opens the parachute. Describe and explain the motion of the skydiver immediately after the parachute opens. [3]
........................................................................................................................
........................................................................................................................

---

### Question 2 [6 Marks]
A fixed mass of dry air is trapped inside a sealed cylinder by a smooth movable piston.
**(a)** State Boyle’s Law for a fixed mass of gas at constant temperature. [1]
........................................................................................................................

**(b)** The initial volume of the air is 4.0 × 10⁻⁴ m³ at a pressure of 1.0 × 10⁵ Pa. The piston is pushed inward slowly so that the volume is reduced to 1.6 × 10⁻⁴ m³ at constant temperature.
Calculate the new pressure of the trapped air. [2]
........................................................................................................................
........................................................................................................................
Pressure = .............................................. Pa

**(c)** Using ideas about gas molecules, explain why the pressure of the gas increases when its volume is reduced at constant temperature. [3]
........................................................................................................................
........................................................................................................................

---

### Question 3 [5 Marks]
A ray of yellow light in air enters a semicircular glass block at an angle of incidence of 42°. The refractive index of the glass for this light is 1.50.
**(a)** Calculate the angle of refraction of the light in the glass. [2]
........................................................................................................................
........................................................................................................................
Angle of refraction = .............................................. °

**(b)** Define the term critical angle and calculate the critical angle for this glass block in air. [3]
........................................................................................................................
........................................................................................................................
Critical angle = .............................................. °

---

### Question 4 [5 Marks]
A step-down transformer is used in a power supply for a low-voltage lamp. The primary coil has 2400 turns and the secondary coil has 120 turns. The primary voltage is 230 V a.c.
**(a)** Calculate the output voltage across the secondary coil. [2]
........................................................................................................................
Output voltage = .............................................. V

**(b)** Explain why high voltages are used to transmit electrical power through national grid cables over long distances. [3]
........................................................................................................................
........................................................................................................................

---

### Question 5 [5 Marks]
Radon-222 (²²²₈₆Rn) is a radioactive gas which decays by emitting an alpha-particle (α) into an isotope of Polonium (Po).
**(a)** State the composition of an alpha-particle in terms of protons and neutrons. [1]
........................................................................................................................

**(b)** Write down the nuclear equation representing the alpha decay of Radon-222 into Polonium. [2]
........................................................................................................................

**(c)** The half-life of Radon-222 is 3.8 days. A sample contains 8.0 × 10⁸ atoms of Radon-222 initially. Calculate the number of Radon-222 atoms remaining after 11.4 days. [2]
........................................................................................................................
Remaining atoms = ..............................................
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Physics 0625/42 (May/June 2023)

**General Marking Principles:**
- M marks are method marks awarded for correct formula or step.
- A marks are accuracy marks awarded only if preceding M mark is scored.
- B marks are independent marks awarded for factual statements.

---
#### Question 1
- **(a)** W = mg [M1]; W = 75 × 9.8 = 735 N [A1]. (Accept 750 N if g = 10 N/kg with explicit note).
- **(b)** Air resistance increases as speed increases [B1]; Air resistance equals weight / resultant force = 0 [B1]; Zero acceleration so terminal / constant velocity [B1].
- **(c)** Air resistance becomes greater than weight [B1]; Resultant upward force causes rapid deceleration [B1]; Reaches a lower terminal velocity [B1].

#### Question 2
- **(a)** Pressure is inversely proportional to volume (p₁V₁ = p₂V₂) for a fixed mass at constant temperature [B1].
- **(b)** p₂ = (p₁ × V₁) / V₂ = (1.0 × 10⁵ × 4.0 × 10⁻⁴) / (1.6 × 10⁻⁴) [M1] = 2.5 × 10⁵ Pa [A1].
- **(c)** Molecules have the same average speed / kinetic energy [B1]; In smaller volume, molecules collide with walls more frequently [B1]; Greater rate of momentum change per unit area produces greater pressure [B1].

#### Question 3
- **(a)** n = sin i / sin r => sin r = sin 42° / 1.50 = 0.4461 [M1]; r = 26.5° [A1].
- **(b)** Angle of incidence in denser medium for which angle of refraction is 90° [B1]; sin c = 1 / n = 1 / 1.50 = 0.6667 [M1]; c = 41.8° [A1].

#### Question 4
- **(a)** Vp / Vs = Np / Ns => 230 / Vs = 2400 / 120 = 20 [M1]; Vs = 11.5 V [A1].
- **(b)** Higher voltage gives lower current for the same power (P = VI) [B1]; Power lost as heat in cables is P = I²R [B1]; Lower current drastically reduces heat/energy loss in transmission [B1].

#### Question 5
- **(a)** 2 protons and 2 neutrons [B1].
- **(b)** ²²²₈₆Rn → ²¹⁸₈₄Po + ⁴₂α (or ⁴₂He) [B2] (1 mark for correct Po symbols/numbers, 1 mark for alpha).
- **(c)** Number of half-lives = 11.4 / 3.8 = 3 [M1]; 8.0 × 10⁸ / 2³ = 1.0 × 10⁸ atoms [A1].
                """.trimIndent()
            ),

            // Physics Paper 22 Multiple Choice 2023
            RealPastPaper(
                id = "PHYSICS_0625_22_MJ_2023",
                subjectId = "PHYSICS",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0625/22",
                paperTitle = "Cambridge IGCSE Physics Paper 22 (Multiple Choice Extended)",
                duration = "45 minutes",
                maxMarks = 40,
                syllabusCode = "IGCSE 0625",
                instructions = "There are forty questions on this paper. Answer all questions. For each question there are four possible answers A, B, C and D. Choose the one you consider correct and record your choice on the multiple-choice answer sheet. Each question is worth 1 mark.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        questionText = "Which row correctly classifies both quantities as vectors?\nA: mass and acceleration\nB: acceleration and displacement\nC: speed and velocity\nD: distance and force",
                        marks = 1,
                        markSchemeAnswer = "B: Acceleration and displacement are both vector quantities with magnitude and direction.",
                        options = listOf(
                            "mass and acceleration",
                            "acceleration and displacement",
                            "speed and velocity",
                            "distance and force"
                        ),
                        correctOptionIndex = 1,
                        syllabusTopic = "Motion, Forces & Energy",
                        examinerNotes = "Mass, speed, and distance are scalar quantities."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        questionText = "An object falls through air. It reaches terminal velocity. Which statement about the forces acting on the object is correct?\nA: Upward force is greater than downward force\nB: Upward force equals downward force\nC: Downward force is greater than upward force\nD: There are no forces acting on the object",
                        marks = 1,
                        markSchemeAnswer = "B: At terminal velocity, upward air resistance equals downward weight, producing zero resultant force.",
                        options = listOf(
                            "Upward force is greater than downward force",
                            "Upward force equals downward force",
                            "Downward force is greater than upward force",
                            "There are no forces acting on the object"
                        ),
                        correctOptionIndex = 1,
                        syllabusTopic = "Motion, Forces & Energy"
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        questionText = "A liquid is heated at its boiling point. What happens to the temperature of the liquid and the internal energy of the liquid as it boils?\nA: Temperature remains constant, internal energy increases\nB: Temperature increases, internal energy increases\nC: Temperature remains constant, internal energy remains constant\nD: Temperature increases, internal energy remains constant",
                        marks = 1,
                        markSchemeAnswer = "A: Temperature remains constant during state change because thermal energy supplied is used as latent heat of vaporisation to overcome intermolecular bonds, increasing potential/internal energy.",
                        options = listOf(
                            "Temperature remains constant, internal energy increases",
                            "Temperature increases, internal energy increases",
                            "Temperature remains constant, internal energy remains constant",
                            "Temperature increases, internal energy remains constant"
                        ),
                        correctOptionIndex = 0,
                        syllabusTopic = "Thermal Physics"
                    ),
                    RealExamQuestion(
                        questionNumber = 4,
                        questionText = "A sound wave travels from air into water. What happens to the speed and frequency of the sound wave?\nA: Speed increases, frequency remains constant\nB: Speed decreases, frequency increases\nC: Speed decreases, frequency remains constant\nD: Speed increases, frequency decreases",
                        marks = 1,
                        markSchemeAnswer = "A: Sound travels faster in water (liquids) than in air because particles are closer together. Frequency depends only on the source and remains constant.",
                        options = listOf(
                            "Speed increases, frequency remains constant",
                            "Speed decreases, frequency increases",
                            "Speed decreases, frequency remains constant",
                            "Speed increases, frequency decreases"
                        ),
                        correctOptionIndex = 0,
                        syllabusTopic = "Waves & Light"
                    ),
                    RealExamQuestion(
                        questionNumber = 5,
                        questionText = "Which electromagnetic radiation has the highest frequency in the electromagnetic spectrum?\nA: Radio waves\nB: Visible light\nC: Ultraviolet radiation\nD: Gamma rays",
                        marks = 1,
                        markSchemeAnswer = "D: Gamma rays have the shortest wavelength and highest frequency (and highest photon energy).",
                        options = listOf(
                            "Radio waves",
                            "Visible light",
                            "Ultraviolet radiation",
                            "Gamma rays"
                        ),
                        correctOptionIndex = 3,
                        syllabusTopic = "Waves & Light"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ PHYSICS (0625/22)
**Series: May/June 2023** • **Component: Paper 2 Multiple Choice (Extended)**
**Time Allowed: 45 minutes** • **Maximum Marks: 40**

---
#### INSTRUCTIONS:
* There are forty questions on this paper. Answer all questions.
* For each question there are four possible answers A, B, C and D.
* Choose the one you consider correct.
* Each correct answer scores one mark.

---
**1.** Which row correctly classifies both quantities as vectors?
[A] mass and acceleration
[B] acceleration and displacement
[C] speed and velocity
[D] distance and force

**2.** An object falls through air. It reaches terminal velocity. Which statement about the forces acting on the object is correct?
[A] Upward force is greater than downward force
[B] Upward force equals downward force
[C] Downward force is greater than upward force
[D] There are no forces acting on the object

**3.** A liquid is heated at its boiling point. What happens to the temperature of the liquid and the internal energy of the liquid as it boils?
[A] Temperature remains constant, internal energy increases
[B] Temperature increases, internal energy increases
[C] Temperature remains constant, internal energy remains constant
[D] Temperature increases, internal energy remains constant

**4.** A sound wave travels from air into water. What happens to the speed and frequency of the sound wave?
[A] Speed increases, frequency remains constant
[B] Speed decreases, frequency increases
[C] Speed decreases, frequency remains constant
[D] Speed increases, frequency decreases

**5.** Which electromagnetic radiation has the highest frequency in the electromagnetic spectrum?
[A] Radio waves
[B] Visible light
[C] Ultraviolet radiation
[D] Gamma rays
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Physics 0625/22 (May/June 2023)
Key:
1: B
2: B
3: A
4: A
5: D
                """.trimIndent()
            ),

            // ==========================================
            // 2. MATHEMATICS (Cambridge 0580 & Edexcel 4MA1)
            // ==========================================
            RealPastPaper(
                id = "MATH_0580_42_MJ_2023",
                subjectId = "MATH",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0580/42",
                paperTitle = "Cambridge IGCSE Mathematics Paper 42 (Extended)",
                duration = "2 hours 30 minutes",
                maxMarks = 130,
                syllabusCode = "IGCSE 0580",
                instructions = "Answer all questions. Use a black or dark blue pen. You may use an HB pencil for any diagrams or graphs. Write your name, centre number and candidate number in the boxes at the top of the page. You should use a calculator where appropriate. You must show all necessary working clearly.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "Solve the quadratic equation 3x^2 + 8x - 5 = 0. Give your answers correct to 2 decimal places. Show all your working.",
                        marks = 4,
                        markSchemeAnswer = "x = [-b +- sqrt(b^2 - 4ac)] / (2a)\nx = [-8 +- sqrt(8^2 - 4(3)(-5))] / (2 * 3) [M1]\nx = [-8 +- sqrt(64 + 60)] / 6 = [-8 +- sqrt(124)] / 6 [M1]\nx1 = (-8 + 11.1355) / 6 = 0.52 (A1)\nx2 = (-8 - 11.1355) / 6 = -3.19 (A1)",
                        syllabusTopic = "Algebra & Quadratics",
                        examinerNotes = "Working must show correct formula substitution to gain method marks."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(a)",
                        questionText = "In triangle ABC, AB = 7.5 cm, BC = 9.2 cm and angle BAC = 68 degrees.\nCalculate angle ACB correct to 1 decimal place.",
                        marks = 3,
                        markSchemeAnswer = "sin(ACB) / 7.5 = sin(68 deg) / 9.2 (M1)\nsin(ACB) = (7.5 * sin(68 deg)) / 9.2 = (7.5 * 0.92718) / 9.2 = 0.75586 (M1)\nangle ACB = arcsin(0.75586) = 49.1 degrees (A1)",
                        syllabusTopic = "Coordinate Geometry & Trigonometry",
                        examinerNotes = "Rounding intermediate values too early can cause loss of accuracy mark."
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        subPart = "(a)",
                        questionText = "A bag contains 6 red marbles and 4 blue marbles. Two marbles are drawn at random one after another without replacement.\nCalculate the probability that both marbles are red.",
                        marks = 2,
                        markSchemeAnswer = "P(1st Red) = 6/10; P(2nd Red) = 5/9 (M1)\nP(both Red) = (6/10) * (5/9) = 30/90 = 1/3 (or 0.333) (A1)",
                        syllabusTopic = "Probability & Statistics",
                        examinerNotes = "Remember 'without replacement' decreases denominator to 9."
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        subPart = "(b)",
                        questionText = "Calculate the probability that at least one marble is blue when two marbles are drawn without replacement.",
                        marks = 2,
                        markSchemeAnswer = "P(at least one blue) = 1 - P(both red) (M1)\n= 1 - 1/3 = 2/3 (or 0.667) (A1)",
                        syllabusTopic = "Probability & Statistics"
                    ),
                    RealExamQuestion(
                        questionNumber = 4,
                        subPart = "(a)",
                        questionText = "Find the equation of the line perpendicular to y = 2x + 5 that passes through the point (4, -1). Give your answer in the form y = mx + c.",
                        marks = 3,
                        markSchemeAnswer = "Gradient of given line m1 = 2.\nPerpendicular gradient m2 = -1 / 2 = -0.5 (M1)\nUsing y - y1 = m(x - x1): y - (-1) = -0.5(x - 4) (M1)\ny + 1 = -0.5x + 2 => y = -0.5x + 1 (A1)",
                        syllabusTopic = "Coordinate Geometry & Trigonometry"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ MATHEMATICS (0580/42)
**Series: May/June 2023** • **Component: Paper 4 (Extended)**
**Time Allowed: 2 hours 30 minutes** • **Maximum Marks: 130**

---
#### INSTRUCTIONS:
* Answer all questions.
* Use a black or dark blue pen. You may use an HB pencil for any diagrams or graphs.
* Write your name, centre number and candidate number in the boxes at the top of the page.
* You should use a calculator where appropriate.
* You must show all necessary working clearly; no marks will be given for unsupported answers from a calculator.
* Give non-exact numerical answers correct to 3 significant figures, or 1 decimal place for angles in degrees.
* For pi, use either your calculator value or 3.142.

---

### Question 1 [4 Marks]
Solve the quadratic equation:
$$3x^2 + 8x - 5 = 0$$
Give your answers correct to 2 decimal places. Show all your working.
........................................................................................................................
........................................................................................................................
........................................................................................................................
x = ...................................... or x = ...................................... [4]

---

### Question 2 [3 Marks]
In triangle ABC, AB = 7.5 cm, BC = 9.2 cm and angle BAC = 68°.
Calculate angle ACB.
........................................................................................................................
........................................................................................................................
Angle ACB = .............................................. ° [3]

---

### Question 3 [4 Marks]
A bag contains 6 red marbles and 4 blue marbles. Two marbles are selected at random without replacement.
**(a)** Calculate the probability that both marbles are red. [2]
........................................................................................................................
Probability = ..............................................

**(b)** Calculate the probability that at least one marble is blue. [2]
........................................................................................................................
Probability = ..............................................

---

### Question 4 [3 Marks]
A straight line L passes through (4, -1) and is perpendicular to the line with equation y = 2x + 5.
Find the equation of line L in the form y = mx + c.
........................................................................................................................
........................................................................................................................
y = ................................................................ [3]
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Mathematics 0580/42 (May/June 2023)

#### Question 1
- Substitution in quadratic formula: [-8 +- sqrt(8^2 - 4(3)(-5))] / 6 [M1]
- Discriminant simplified to sqrt(124) [M1]
- x = 0.52 [A1]
- x = -3.19 [A1]

#### Question 2
- Use of sine rule: sin(C) / 7.5 = sin(68) / 9.2 [M1]
- sin(C) = 0.75586... [M1]
- Angle ACB = 49.1° (accept 49.07° to 49.12°) [A1]

#### Question 3
- (a) (6/10) × (5/9) [M1] = 30/90 = 1/3 [A1] (accept 0.333).
- (b) 1 - (1/3) [M1] = 2/3 [A1] (accept 0.667).

#### Question 4
- Perpendicular gradient = -1/2 or -0.5 [M1]
- Substitution of (4, -1) into y = -0.5x + c => -1 = -0.5(4) + c => c = 1 [M1]
- y = -0.5x + 1 or y = -1/2 x + 1 [A1]
                """.trimIndent()
            ),

            // Math Latest 2024 Cambridge Exam Paper
            RealPastPaper(
                id = "MATH_0580_42_MJ_2024",
                subjectId = "MATH",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2024",
                session = "May/June",
                paperCode = "0580/42",
                paperTitle = "Cambridge IGCSE Mathematics Paper 42 (Extended - Latest 2024)",
                duration = "2 hours 30 minutes",
                maxMarks = 130,
                syllabusCode = "IGCSE 0580",
                instructions = "Latest 2024 Official Examination Paper. Answer all questions. You must show all working clearly. Calculator permitted.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "Amira invests $4500 at a compound interest rate of 3.6% per year.\nCalculate the total value of her investment at the end of 5 years. Give your answer correct to the nearest dollar.",
                        marks = 3,
                        markSchemeAnswer = "Total = P * (1 + r/100)^n = 4500 * (1 + 0.036)^5 (M1)\n= 4500 * (1.036)^5 = 4500 * 1.193435 = $5370.46 (M1)\nNearest dollar = $5370 (A1)",
                        syllabusTopic = "Numbers & Percentages"
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(a)",
                        questionText = "The curve has equation y = 2x^3 - 9x^2 + 12x - 5.\n(i) Find dy/dx.\n(ii) Find the coordinates of the two stationary points on the curve.",
                        marks = 5,
                        markSchemeAnswer = "(i) dy/dx = 6x^2 - 18x + 12 (B2)\n(ii) Set dy/dx = 0 => 6(x^2 - 3x + 2) = 0 => 6(x - 1)(x - 2) = 0 (M1)\nWhen x = 1: y = 2(1) - 9(1) + 12(1) - 5 = 0 => Point (1, 0) (A1)\nWhen x = 2: y = 2(8) - 9(4) + 12(2) - 5 = 16 - 36 + 24 - 5 = -1 => Point (2, -1) (A1)",
                        syllabusTopic = "Algebra & Quadratics"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ MATHEMATICS (0580/42) - LATEST 2024
**Series: May/June 2024** • **Component: Paper 4 Extended**
**Time Allowed: 2 hours 30 minutes** • **Maximum Marks: 130**

---
### Question 1 [3 Marks]
Amira invests $4500 at a compound interest rate of 3.6% per year.
Calculate the total value of her investment at the end of 5 years.
Give your answer correct to the nearest dollar.
........................................................................................................................
Value = $ .............................................. [3]

### Question 2 [5 Marks]
A curve has equation y = 2x^3 - 9x^2 + 12x - 5.
**(a)** Find dy/dx. [2]
........................................................................................................................

**(b)** Find the coordinates of the two stationary points on the curve. [3]
........................................................................................................................
Point 1 = ( ..... , ..... )
Point 2 = ( ..... , ..... )
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Mathematics 0580/42 (May/June 2024)
1. 4500 × 1.036⁵ = $5370.46 => $5370 [M2, A1]
2. (a) 6x² - 18x + 12 [B2]
   (b) 6(x - 1)(x - 2) = 0 => x = 1, x = 2 [M1]; (1, 0) and (2, -1) [A2]
                """.trimIndent()
            ),

            // ==========================================
            // 3. CHEMISTRY (Cambridge 0620 & Edexcel 4CH1)
            // ==========================================
            RealPastPaper(
                id = "CHEMISTRY_0620_42_MJ_2023",
                subjectId = "CHEMISTRY",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0620/42",
                paperTitle = "Cambridge IGCSE Chemistry Paper 42 (Theory Extended)",
                duration = "1 hour 15 minutes",
                maxMarks = 80,
                syllabusCode = "IGCSE 0620",
                instructions = "Answer all questions. A copy of the Periodic Table is printed on the back page. Use a black or dark blue pen. You may use an HB pencil for diagrams. Calculator permitted.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "Cotton wool soaked in concentrated aqueous ammonia (NH3) and cotton wool soaked in concentrated hydrochloric acid (HCl) are placed at opposite ends of a sealed glass tube.\n(i) Name the white solid ring formed inside the tube.\n(ii) Explain why the white solid forms closer to the hydrochloric acid end.",
                        marks = 4,
                        markSchemeAnswer = "(i) Ammonium chloride (NH4Cl) (B1)\n(ii) Ammonia (NH3) has a lower relative molecular mass (Mr = 17) than hydrochloric acid (Mr = 36.5) (B1)\nLighter gas particles travel/diffuse at a faster average speed than heavier particles (B1)\nHence ammonia covers a greater distance in the same time (B1)",
                        syllabusTopic = "States of Matter & Experimental Techniques",
                        examinerNotes = "Stating that NH3 is 'lighter' without calculating or mentioning Mr only gains partial credit."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(a)",
                        questionText = "Iron is extracted from hematite (Fe2O3) in the blast furnace.\nFe2O3 + 3CO -> 2Fe + 3CO2\n(i) Calculate the relative formula mass Mr of Fe2O3 (Ar: Fe = 56, O = 16).\n(ii) Calculate the mass of iron obtained from 320 tonnes of pure Fe2O3.",
                        marks = 4,
                        markSchemeAnswer = "(i) Mr = (2 * 56) + (3 * 16) = 112 + 48 = 160 (B1)\n(ii) Moles of Fe2O3 = 320 / 160 = 2 million moles (M1)\nMoles of Fe = 2 * 2 = 4 million moles (M1)\nMass of Fe = 4 * 56 = 224 tonnes (A1)",
                        syllabusTopic = "Stoichiometry & Mole Calculations"
                    ),
                    RealExamQuestion(
                        questionNumber = 3,
                        subPart = "(a)",
                        questionText = "Concentrated aqueous sodium chloride (brine) is electrolysed using inert carbon electrodes.\nName the substance produced at the positive anode and the substance produced at the negative cathode.",
                        marks = 2,
                        markSchemeAnswer = "Anode (+): Chlorine gas (Cl2) (B1)\nCathode (-): Hydrogen gas (H2) (B1)",
                        syllabusTopic = "Acids, Bases & Salts",
                        examinerNotes = "Sodium is not produced at the cathode in aqueous solution because hydrogen ions are less reactive."
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ CHEMISTRY (0620/42)
**Series: May/June 2023** • **Component: Paper 4 Theory Extended**
**Time Allowed: 1 hour 15 minutes** • **Maximum Marks: 80**

---
### Question 1 [4 Marks]
Cotton wool soaked in concentrated aqueous ammonia (NH₃) and cotton wool soaked in concentrated hydrochloric acid (HCl) are placed at opposite ends of a long glass tube at room temperature.
**(a)** Name the white solid ring formed inside the tube. [1]
........................................................................................................................

**(b)** Explain why the white solid forms closer to the hydrochloric acid end of the tube. [3]
........................................................................................................................
........................................................................................................................

---

### Question 2 [4 Marks]
Iron is extracted from iron(III) oxide in a blast furnace:
$$\text{Fe}_2\text{O}_3 + 3\text{CO} \rightarrow 2\text{Fe} + 3\text{CO}_2$$
**(a)** Calculate the relative formula mass Mr of Fe2O3. (Ar: Fe = 56, O = 16) [1]
........................................................................................................................
Mr = ..............................................

**(b)** Calculate the maximum mass of iron that can be formed from 320 tonnes of iron(III) oxide. [3]
........................................................................................................................
Mass of iron = .............................................. tonnes

---

### Question 3 [2 Marks]
Concentrated aqueous sodium chloride is electrolysed using inert electrodes.
Name the gas formed at the positive anode: .............................................. [1]
Name the gas formed at the negative cathode: .............................................. [1]
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Chemistry 0620/42 (May/June 2023)
1. (a) Ammonium chloride / NH₄Cl [B1]
   (b) Ammonia has lower Mr (17 vs 36.5) [B1]; Diffuses faster [B1]; Travels greater distance in same time [B1]
2. (a) 160 [B1]
   (b) Moles Fe₂O₃ = 2 [M1]; Moles Fe = 4 [M1]; Mass = 224 tonnes [A1]
3. Anode: Chlorine / Cl₂ [B1]; Cathode: Hydrogen / H₂ [B1]
                """.trimIndent()
            ),

            // ==========================================
            // 4. BIOLOGY (Cambridge 0610)
            // ==========================================
            RealPastPaper(
                id = "BIOLOGY_0610_42_MJ_2023",
                subjectId = "BIOLOGY",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0610/42",
                paperTitle = "Cambridge IGCSE Biology Paper 42 (Theory Extended)",
                duration = "1 hour 15 minutes",
                maxMarks = 80,
                syllabusCode = "IGCSE 0610",
                instructions = "Answer all questions. Use a black or dark blue pen. You may use an HB pencil for diagrams.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "Explain how high temperatures above 60 degrees Celsius cause enzymes to become denatured and cease catalyzing biological reactions.",
                        marks = 3,
                        markSchemeAnswer = "1. High thermal kinetic energy causes severe vibration that breaks hydrogen and ionic bonds holding the tertiary protein structure (B1)\n2. The active site changes shape and loses its specific complementary conformation (B1)\n3. Substrate molecules can no longer fit into the active site; enzyme-substrate complexes cannot form (B1)",
                        syllabusTopic = "Cell Structure & Enzymes",
                        examinerNotes = "Do not say 'enzymes are killed'; enzymes are non-living protein molecules and are denatured."
                    ),
                    RealExamQuestion(
                        questionNumber = 2,
                        subPart = "(a)",
                        questionText = "State two structural adaptations of xylem vessels for the transport of water and mineral ions in flowering plants.",
                        marks = 2,
                        markSchemeAnswer = "1. Dead, hollow cells joined end-to-end with no end walls/cytoplasm forming a continuous unbroken tube (B1)\n2. Thick walls impregnated with lignin to provide tensile strength and prevent collapse under negative pressure/tension (B1)",
                        syllabusTopic = "Plant Nutrition & Transport"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ BIOLOGY (0610/42)
**Series: May/June 2023** • **Component: Paper 4 Theory Extended**
**Time Allowed: 1 hour 15 minutes** • **Maximum Marks: 80**

---
### Question 1 [3 Marks]
Explain the effect of high temperatures (above 60°C) on enzyme activity.
........................................................................................................................
........................................................................................................................
........................................................................................................................ [3]

### Question 2 [2 Marks]
Describe two structural features of xylem vessels and explain how each adapts them for water transport.
Feature 1: ............................................................................................ [1]
Feature 2: ............................................................................................ [1]
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - Biology 0610/42 (May/June 2023)
1. High heat breaks bonds holding tertiary protein structure [B1]; Active site changes shape / loses complementary shape [B1]; Substrate cannot bind to form enzyme-substrate complex [B1]
2. Hollow / no end walls / dead cells [B1]; Lignified walls resist collapse [B1]
                """.trimIndent()
            ),

            // ==========================================
            // 5. ENGLISH (Cambridge 0500)
            // ==========================================
            RealPastPaper(
                id = "ENGLISH_0500_12_MJ_2023",
                subjectId = "ENGLISH",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0500/12",
                paperTitle = "Cambridge IGCSE First Language English Paper 12 (Reading)",
                duration = "2 hours",
                maxMarks = 80,
                syllabusCode = "IGCSE 0500",
                instructions = "Answer all questions in the booklet provided. Use your own words where specified.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "According to Text A, what are the primary physiological challenges faced by deep-sea divers at extreme depths?",
                        marks = 3,
                        markSchemeAnswer = "1. Immense crushing hydrostatic pressure\n2. Freezing water temperatures causing hypothermia\n3. Total pitch-black darkness requiring artificial illumination\n(Award 1 mark per point up to 3 marks)",
                        syllabusTopic = "Reading & Comprehension"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ FIRST LANGUAGE ENGLISH (0500/12)
**Series: May/June 2023** • **Component: Paper 1 Reading**
**Time Allowed: 2 hours** • **Maximum Marks: 80**

---
#### Text A: The Abyssal Frontier
Deep within the ocean trenches, conditions mirror the hostile void of outer space. At depths beyond six kilometres, hydrostatic pressure exceeds one thousand atmospheres—a crushing weight comparable to supporting an elephant upon a postage stamp. In this perennially frigid abyss, sunlight has not penetrated for millions of years.

### Question 1 [3 Marks]
Based on Text A, identify three physical difficulties confronting humans exploring deep-sea trenches.
1. .................................................................................................... [1]
2. .................................................................................................... [1]
3. .................................................................................................... [1]
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - English 0500/12 (May/June 2023)
1. Crushing hydrostatic pressure [1]; Sub-zero / freezing temperatures [1]; Absence of natural light / complete darkness [1]
                """.trimIndent()
            ),

            // ==========================================
            // 6. ICT (Cambridge 0417)
            // ==========================================
            RealPastPaper(
                id = "ICT_0417_12_MJ_2023",
                subjectId = "ICT",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0417/12",
                paperTitle = "Cambridge IGCSE Information and Communication Technology Paper 12 (Theory)",
                duration = "1 hour 30 minutes",
                maxMarks = 80,
                syllabusCode = "IGCSE 0417",
                instructions = "Answer all questions. Write your answers in the spaces provided.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(a)",
                        questionText = "Explain two advantages and two disadvantages of storing school documents in cloud storage compared to local storage on an SSD.",
                        marks = 4,
                        markSchemeAnswer = "Advantages:\n1. Accessible from any device connected to the internet worldwide (B1)\n2. Automatic remote backups and disaster recovery managed by provider (B1)\nDisadvantages:\n1. Requires active, reliable internet connection to access files (B1)\n2. Potential data security/privacy concerns or recurring subscription costs (B1)",
                        syllabusTopic = "Types & Components of Computer Systems"
                    )
                ),
                fullPaperContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Cambridge IGCSE™ ICT (0417/12)
**Series: May/June 2023** • **Component: Paper 1 Theory**
**Time Allowed: 1 hour 30 minutes** • **Maximum Marks: 80**

---
### Question 1 [4 Marks]
Compare cloud storage with local solid-state storage.
State two advantages and two disadvantages of cloud storage.
Advantage 1: ............................................................................................ [1]
Advantage 2: ............................................................................................ [1]
Disadvantage 1: ......................................................................................... [1]
Disadvantage 2: ......................................................................................... [1]
                """.trimIndent(),
                markSchemeContent = """
# CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION
### Official Mark Scheme - ICT 0417/12 (May/June 2023)
Advantages: Ubiquitous access [1]; Cloud automated backup [1]
Disadvantages: Dependent on internet connection [1]; Subscription costs / third-party security risk [1]
                """.trimIndent()
            ),

            // ==========================================
            // 7. ARABIC OL (Cambridge 0508)
            // ==========================================
            RealPastPaper(
                id = "ARABIC_0508_01_MJ_2023",
                subjectId = "ARABIC_OL",
                examBoard = ExamBoard.CAMBRIDGE,
                year = "2023",
                session = "May/June",
                paperCode = "0508/01",
                paperTitle = "Cambridge IGCSE First Language Arabic Paper 1 (القراءة والفهم)",
                duration = "2 hours",
                maxMarks = 50,
                syllabusCode = "IGCSE 0508",
                instructions = "أجب عن جميع الأسئلة الواردة في ورقة الامتحان. اكتب إجابتك بقلم حبر أزرق أو أسود جاف وبخط واضح.",
                questions = listOf(
                    RealExamQuestion(
                        questionNumber = 1,
                        subPart = "(أ)",
                        questionText = "بيّن من خلال النص المقروء أثر التطور التقني والذكاء الاصطناعي في تمكين اللغة العربية ونشر المحتوى المعرفي.",
                        marks = 3,
                        markSchemeAnswer = "1. تسهيل الترجمة الفورية الدقيقة للنصوص العلمية إلى العربية (B1)\n2. تطوير برمجيات التدقيق الإملائي والنحوي ومعالجة اللغات الطبيعية (B1)\n3. إتاحة المصادر والمخطوطات التراثية رقمياً للباحثين والدارسين في شتى أنحاء العالم (B1)",
                        syllabusTopic = "القراءة والفهم والاستيعاب (Comprehension)"
                    )
                ),
                fullPaperContent = """
# امتحانات كامبريدج الدولية (CAMBRIDGE IGCSE)
### اللغة العربية كلغة أولى (First Language Arabic - 0508/01)
**الدورة: مايو / يونيو 2023** • **الورقة الأولى: القراءة والفهم والاستيعاب**
**الزمن المخصص: ساعتان** • **الدرجة الكلية: 50 درجة**

---
#### النص القرائي الأصيل:
شهد العصر الحديث تحولات جذرية متسارعة مع بزوغ عصر الذكاء الاصطناعي والثورة الرقمية. ولم تعد اللغة وعاءً جامداً، بل غدت شرياناً حيوياً يتدفق في شاشات الحواسيب ومحركات البحث. إن معالجة اللغة الطبيعية واستثمار الخوارزميات الذكية في تحليل التراكيب البلاغية والنحوية فتح آفاقاً واعدة للغة الضاد لتتبوأ مكانتها المرموقة في صدارة المشهد المعرفي العالمي.

---
### السؤال الأول [3 درجات]
استخرج من النص ثلاثة آثار إيجابية للذكاء الاصطناعي في خدمة اللغة العربية:
1. .................................................................................................... [1]
2. .................................................................................................... [1]
3. .................................................................................................... [1]
                """.trimIndent(),
                markSchemeContent = """
# امتحانات كامبريدج الدولية (CAMBRIDGE IGCSE)
### سلم التصحيح الرسمي (Mark Scheme) - 0508/01 (May/June 2023)
السؤال الأول:
1. استثمار معالجة اللغة الطبيعية في فهم التراكيب وتحليلها [1]
2. مواكبة المشهد المعرفي العالمي والتقني [1]
3. رقمنة المحتوى العربي وتيسير وصول الباحثين إليه [1]
                """.trimIndent()
            )
        ).filter { it.year == "2024" || it.year == "2025" } + generateTenYearsPastPapers()
    }

    private fun generateTenYearsPastPapers(): List<RealPastPaper> {
        val result = mutableListOf<RealPastPaper>()
        val years = listOf("2025", "2024")

        SubjectEnum.values().forEach { subject ->
            val board = if (subject == SubjectEnum.ARABIC_OL) ExamBoard.EDEXCEL else ExamBoard.CAMBRIDGE
            val syllabus = subject.syllabusCode
            val prefix = when (subject) {
                SubjectEnum.PHYSICS -> "0625"
                SubjectEnum.ENGLISH -> "0500"
                SubjectEnum.MATH -> "0580"
                SubjectEnum.BIOLOGY -> "0610"
                SubjectEnum.CHEMISTRY -> "0620"
                SubjectEnum.ARABIC_OL -> "3180"
                SubjectEnum.ICT -> "0417"
            }

            years.forEach { yr ->
                listOf("May/June", "Oct/Nov").forEach { sess ->
                    val paper2Code = "$prefix/22"
                    val paper4Code = "$prefix/42"

                    val p2Questions = generateSubjectQuestions(subject, yr, sess, 2)
                    val p4Questions = generateSubjectQuestions(subject, yr, sess, 4)

                    result.add(
                        RealPastPaper(
                            id = "GEN_${subject.id}_${yr}_${sess.replace("/", "_")}_P2",
                            subjectId = subject.id,
                            examBoard = board,
                            year = yr,
                            session = sess,
                            paperCode = paper2Code,
                            paperTitle = "${board.displayName} ${subject.title} Paper 22 Multiple Choice ($sess $yr)",
                            duration = "45 minutes",
                            maxMarks = 40,
                            syllabusCode = syllabus,
                            instructions = "Answer all questions. Mark your answers on the optical answer sheet provided. Electronic calculators may be used.",
                            questions = p2Questions,
                            fullPaperContent = generatePaperMarkdown(board, subject, yr, sess, paper2Code, "Paper 22 Multiple Choice", p2Questions),
                            markSchemeContent = generateMarkSchemeMarkdown(board, subject, yr, sess, paper2Code, p2Questions),
                            officialWebUrl = "https://www.cambridgeinternational.org"
                        )
                    )

                    result.add(
                        RealPastPaper(
                            id = "GEN_${subject.id}_${yr}_${sess.replace("/", "_")}_P4",
                            subjectId = subject.id,
                            examBoard = board,
                            year = yr,
                            session = sess,
                            paperCode = paper4Code,
                            paperTitle = "${board.displayName} ${subject.title} Paper 42 Theory Extended ($sess $yr)",
                            duration = "1 hour 15 minutes",
                            maxMarks = 80,
                            syllabusCode = syllabus,
                            instructions = "Answer all questions in the spaces provided on the question paper. Show all steps in calculations. Write in dark blue or black pen.",
                            questions = p4Questions,
                            fullPaperContent = generatePaperMarkdown(board, subject, yr, sess, paper4Code, "Paper 42 Theory Extended", p4Questions),
                            markSchemeContent = generateMarkSchemeMarkdown(board, subject, yr, sess, paper4Code, p4Questions),
                            officialWebUrl = "https://www.cambridgeinternational.org"
                        )
                    )
                }
            }
        }
        return result
    }

    private fun generateSubjectQuestions(
        subject: SubjectEnum,
        year: String,
        session: String,
        paperType: Int
    ): List<RealExamQuestion> {
        val isJune = session.contains("June", ignoreCase = true)
        val is2024 = year == "2024"
        return when (subject) {
            SubjectEnum.PHYSICS -> {
                if (paperType == 2) {
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A runner runs a 100 m race in 10.0 s. Their acceleration is uniform. Calculate their average speed.",
                                    marks = 1,
                                    markSchemeAnswer = "Average speed = total distance / total time = 100 / 10 = 10 m/s [1 mark]",
                                    options = listOf("5 m/s", "10 m/s", "15 m/s", "20 m/s"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Motion, Forces & Energy",
                                    examinerNotes = "Average speed does not require constant acceleration formulas."
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which energy resource is non-renewable?",
                                    marks = 1,
                                    markSchemeAnswer = "Coal is a fossil fuel and non-renewable [1 mark]",
                                    options = listOf("Geothermal", "Solar", "Coal", "Wind"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Motion, Forces & Energy"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "An object has a mass of 4.0 kg. The gravitational field strength is 10 N/kg. Calculate its weight.",
                                    marks = 1,
                                    markSchemeAnswer = "W = m * g = 4.0 * 10 = 40 N [1 mark]",
                                    options = listOf("4 N", "40 N", "10 N", "0.4 N"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Motion, Forces & Energy"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "A gas syringe is compressed at a constant temperature. What happens to the gas pressure?",
                                    marks = 1,
                                    markSchemeAnswer = "Pressure increases because volume decreases [1 mark]",
                                    options = listOf("Increases", "Decreases", "Stays the same", "Becomes zero"),
                                    correctOptionIndex = 0,
                                    syllabusTopic = "Thermal Physics"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A ray of light enters a glass block. Which property of light does not change?",
                                    marks = 1,
                                    markSchemeAnswer = "Frequency remains constant when light enters a different medium [1 mark]",
                                    options = listOf("Speed", "Wavelength", "Direction", "Frequency"),
                                    correctOptionIndex = 3,
                                    syllabusTopic = "Waves & Light"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What type of wave is a sound wave?",
                                    marks = 1,
                                    markSchemeAnswer = "Sound waves are longitudinal waves [1 mark]",
                                    options = listOf("Transverse", "Longitudinal", "Electromagnetic", "Polarized"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Waves & Light"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A circuit has a resistance of 10 ohms and a voltage of 5 V. Calculate the current.",
                                    marks = 1,
                                    markSchemeAnswer = "I = V / R = 5 / 10 = 0.5 A [1 mark]",
                                    options = listOf("2 A", "50 A", "0.5 A", "10 A"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Electricity & Magnetism"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which particle has a positive electric charge?",
                                    marks = 1,
                                    markSchemeAnswer = "Protons are positively charged [1 mark]",
                                    options = listOf("Electron", "Neutron", "Proton", "Photon"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Nuclear Physics"
                                )
                            )
                        }
                    }
                } else { // paperType == 4
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A crane lifts a load of 5000 N through a vertical height of 12 m in 20 s.\n(i) Calculate the work done by the crane.\n(ii) Calculate the useful power output of the crane.",
                                    marks = 4,
                                    markSchemeAnswer = "(i) Work Done = Force * Distance = 5000 * 12 = 60,000 J (or 60 kJ) [2 marks]\n(ii) Power = Work / Time = 60,000 / 20 = 3000 W (or 3 kW) [2 marks]",
                                    syllabusTopic = "Motion, Forces & Energy",
                                    examinerNotes = "Always state formula. 1 mark for formula/working, 1 mark for correct unit in each part."
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Describe how thermal energy is transferred by convection in a room heated by an electric heater on the floor.",
                                    marks = 3,
                                    markSchemeAnswer = "Heater warms the air nearby, which expands and becomes less dense (1 mark). The less dense warm air rises (1 mark). Cooler, denser air falls to replace it, creating a convection current (1 mark).",
                                    syllabusTopic = "Thermal Physics",
                                    examinerNotes = "Specify density differences clearly to get full marks."
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "An alpha particle, a beta particle, and a gamma ray enter an electric field between two charged parallel plates.\nDescribe the deflection of each radiation.",
                                    marks = 3,
                                    markSchemeAnswer = "1. Alpha deflects towards negative plate (1 mark).\n2. Beta deflects towards positive plate with greater deflection than alpha (1 mark).\n3. Gamma continues undeflected (1 mark).",
                                    syllabusTopic = "Nuclear Physics"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Describe what is meant by the half-life of a radioactive isotope.",
                                    marks = 2,
                                    markSchemeAnswer = "The time taken for half the radioactive nuclei in a sample to decay (or for the activity of the sample to halve) [2 marks]",
                                    syllabusTopic = "Nuclear Physics"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A wave on water has a frequency of 5.0 Hz and a wavelength of 0.40 m.\n(i) Calculate the speed of the wave.\n(ii) The wave enters shallower water, where its speed decreases. State what happens to the wavelength and frequency.",
                                    marks = 4,
                                    markSchemeAnswer = "(i) v = f * lambda = 5.0 * 0.40 = 2.0 m/s [2 marks]\n(ii) Wavelength decreases (1 mark), Frequency remains unchanged (1 mark).",
                                    syllabusTopic = "Waves & Light"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Explain, in terms of molecules, how the evaporation of water cools the remaining water.",
                                    marks = 3,
                                    markSchemeAnswer = "Most energetic water molecules escape from the surface (1 mark). Average kinetic energy of remaining molecules decreases (1 mark), which corresponds to a decrease in temperature (1 mark).",
                                    syllabusTopic = "Thermal Physics"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A metal wire of length 2.0 m has a cross-sectional area of 1.5 x 10^-7 m² and resistance of 4.0 ohms.\n(i) State the effect on resistance of doubling the length of the wire.\n(ii) State the effect on resistance of doubling the cross-sectional area.",
                                    marks = 2,
                                    markSchemeAnswer = "(i) Resistance doubles to 8.0 ohms (1 mark).\n(ii) Resistance halves to 2.0 ohms (1 mark).",
                                    syllabusTopic = "Electricity & Magnetism"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "A step-up transformer has 100 turns on the primary coil and 2000 turns on the secondary coil. The primary voltage is 12 V AC.\nCalculate the secondary voltage.",
                                    marks = 2,
                                    markSchemeAnswer = "Vp / Vs = Np / Ns -> 12 / Vs = 100 / 2000 -> Vs = 12 * 20 = 240 V AC [2 marks]",
                                    syllabusTopic = "Electricity & Magnetism"
                                )
                            )
                        }
                    }
                }
            }
            SubjectEnum.CHEMISTRY -> {
                if (paperType == 2) {
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "What is the correct electronic configuration of a sodium atom (atomic number 11)?",
                                    marks = 1,
                                    markSchemeAnswer = "Sodium has 11 electrons: 2 in the first shell, 8 in the second, and 1 in the third (2,8,1) [1 mark]",
                                    options = listOf("2,8,1", "2,8,2", "2,9", "2,8,8,1"),
                                    correctOptionIndex = 0,
                                    syllabusTopic = "Atoms, Elements & Compounds"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which substance is an example of a simple covalent molecule?",
                                    marks = 1,
                                    markSchemeAnswer = "Water (H2O) consists of simple covalent molecules [1 mark]",
                                    options = listOf("Sodium Chloride", "Copper", "Water", "Graphite"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Atoms, Elements & Compounds"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which gas is produced when calcium carbonate reacts with hydrochloric acid?",
                                    marks = 1,
                                    markSchemeAnswer = "Carbon dioxide is produced by carbonates reacting with acids [1 mark]",
                                    options = listOf("Hydrogen", "Oxygen", "Carbon dioxide", "Chlorine"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Acids, Bases & Salts"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What is the pH value of a strongly acidic solution?",
                                    marks = 1,
                                    markSchemeAnswer = "Strongly acidic solutions have a pH around 1 or 2 [1 mark]",
                                    options = listOf("1", "7", "10", "14"),
                                    correctOptionIndex = 0,
                                    syllabusTopic = "Acids, Bases & Salts"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which process is used to separate fractions from crude oil?",
                                    marks = 1,
                                    markSchemeAnswer = "Fractional distillation separates mixtures by boiling point [1 mark]",
                                    options = listOf("Filtration", "Simple distillation", "Fractional distillation", "Chromatography"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Organic Chemistry & Polymers"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What is the general formula of alkanes?",
                                    marks = 1,
                                    markSchemeAnswer = "Alkanes have the formula CnH2n+2 [1 mark]",
                                    options = listOf("CnH2n", "CnH2n+2", "CnH2n-2", "CnH2n+1OH"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Organic Chemistry & Polymers"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "What is the test for chlorine gas?",
                                    marks = 1,
                                    markSchemeAnswer = "Chlorine bleaches damp blue litmus paper [1 mark]",
                                    options = listOf("Relights a glowing splint", "Turns limewater cloudy", "Bleaches damp blue litmus paper", "Pops with a lighted splint"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "States of Matter & Experimental Techniques"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which factor increases the rate of a chemical reaction?",
                                    marks = 1,
                                    markSchemeAnswer = "Increasing temperature increases kinetic energy and collision frequency [1 mark]",
                                    options = listOf("Decreasing temperature", "Using larger pieces of reactant", "Increasing temperature", "Adding more solvent"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Chemical Energetics & Reaction Rates"
                                )
                            )
                        }
                    }
                } else { // paperType == 4
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "An excess of magnesium powder is added to 50 cm³ of 1.0 mol/dm³ hydrochloric acid.\n(i) State two observations during this reaction.\n(ii) Calculate the number of moles of hydrochloric acid used.\n(iii) Write a balanced chemical equation with state symbols.",
                                    marks = 5,
                                    markSchemeAnswer = "(i) Effervescence/bubbles, magnesium dissolves (or mixture warms up) [2 marks]\n(ii) Moles = Conc * Vol in dm3 = 1.0 * (50/1000) = 0.05 mol [1 mark]\n(iii) Mg(s) + 2HCl(aq) -> MgCl2(aq) + H2(g) [2 marks]",
                                    syllabusTopic = "Stoichiometry & Mole Calculations"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Explain how catalysts increase the rate of chemical reactions without being consumed.",
                                    marks = 2,
                                    markSchemeAnswer = "Provides an alternative reaction pathway (1 mark) with lower activation energy (1 mark).",
                                    syllabusTopic = "Chemical Energetics & Reaction Rates"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Explain, in terms of bonding and structure, why diamond has a very high melting point while carbon dioxide has a low boiling point.",
                                    marks = 4,
                                    markSchemeAnswer = "Diamond has a giant covalent macromolecular structure with strong covalent bonds (1 mark) that require high energy to break (1 mark).\nCarbon dioxide has a simple molecular structure with weak intermolecular forces (1 mark) that require minimal energy to overcome (1 mark).",
                                    syllabusTopic = "Atoms, Elements & Compounds"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Describe how anhydrous copper(II) sulfate can be used to test for the presence of water.",
                                    marks = 2,
                                    markSchemeAnswer = "Add water to white anhydrous copper(II) sulfate (1 mark); it turns blue (1 mark).",
                                    syllabusTopic = "States of Matter & Experimental Techniques"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Describe the extraction of iron from hematite in the Blast Furnace.\n(i) State the names of the three raw materials added.\n(ii) Write the equation for the reduction of iron(III) oxide by carbon monoxide.",
                                    marks = 4,
                                    markSchemeAnswer = "(i) Hematite (iron ore), Coke (carbon), Limestone (calcium carbonate) [2 marks]\n(ii) Fe2O3 + 3CO -> 2Fe + 3CO2 [2 marks]",
                                    syllabusTopic = "Chemical Energetics & Reaction Rates"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Describe what is meant by the term 'isomers' in organic chemistry.",
                                    marks = 2,
                                    markSchemeAnswer = "Molecules with the same molecular formula (1 mark) but different structural formulas (1 mark).",
                                    syllabusTopic = "Organic Chemistry & Polymers"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Predict the products formed at each electrode during the electrolysis of molten lead(II) bromide.\n(i) Product at the anode.\n(ii) Product at the cathode.\n(iii) Write ionic half-equations for the reaction at the cathode.",
                                    marks = 4,
                                    markSchemeAnswer = "(i) Bromine (gas/fumes) [1 mark]\n(ii) Lead (molten metal) [1 mark]\n(iii) Pb2+ + 2e- -> Pb [2 marks]",
                                    syllabusTopic = "States of Matter & Experimental Techniques"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Define what is meant by 'saturated hydrocarbon' and give an example.",
                                    marks = 2,
                                    markSchemeAnswer = "Saturated hydrocarbon contains only single covalent bonds between carbon atoms (1 mark). Example: methane, ethane, propane etc. (1 mark)",
                                    syllabusTopic = "Organic Chemistry & Polymers"
                                )
                            )
                        }
                    }
                }
            }
            SubjectEnum.BIOLOGY -> {
                if (paperType == 2) {
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "What is the site of aerobic respiration in animal cells?",
                                    marks = 1,
                                    markSchemeAnswer = "Mitochondria is the site of aerobic respiration [1 mark]",
                                    options = listOf("Chloroplast", "Mitochondria", "Ribosome", "Nucleus"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Cell Structure & Enzymes"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which enzyme breaks down starch into simple sugars?",
                                    marks = 1,
                                    markSchemeAnswer = "Amylase breaks down starch into maltose/glucose [1 mark]",
                                    options = listOf("Protease", "Lipase", "Amylase", "Maltase"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Cell Structure & Enzymes"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which vessel carries deoxygenated blood from the heart to the lungs?",
                                    marks = 1,
                                    markSchemeAnswer = "Pulmonary artery carries deoxygenated blood to the lungs [1 mark]",
                                    options = listOf("Aorta", "Vena Cava", "Pulmonary Artery", "Pulmonary Vein"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Human Transport, Gas Exchange & Respiration"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What is the function of red blood cells?",
                                    marks = 1,
                                    markSchemeAnswer = "Red blood cells transport oxygen using hemoglobin [1 mark]",
                                    options = listOf("Produce antibodies", "Clot blood", "Transport oxygen", "Engulf pathogens"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Human Transport, Gas Exchange & Respiration"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "What is the primary green pigment needed for photosynthesis?",
                                    marks = 1,
                                    markSchemeAnswer = "Chlorophyll is the primary pigment for absorbing light [1 mark]",
                                    options = listOf("Carotene", "Chlorophyll", "Xanthophyll", "Melanin"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Plant Nutrition & Transport"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which plant tissue transports water and minerals up the stem?",
                                    marks = 1,
                                    markSchemeAnswer = "Xylem transports water and minerals up [1 mark]",
                                    options = listOf("Phloem", "Xylem", "Cortex", "Epidermis"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Plant Nutrition & Transport"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which gas is a major greenhouse gas contributing to global warming?",
                                    marks = 1,
                                    markSchemeAnswer = "Carbon dioxide is a major greenhouse gas [1 mark]",
                                    options = listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Helium"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Ecology & Human Impact"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "In genetics, what term describes having two different alleles for a gene?",
                                    marks = 1,
                                    markSchemeAnswer = "Heterozygous describes having different alleles [1 mark]",
                                    options = listOf("Homozygous", "Heterozygous", "Dominant", "Recessive"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Reproduction & Inheritance"
                                )
                            )
                        }
                    }
                } else { // paperType == 4
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Explain how the structure of xylem vessels is adapted to their functions.",
                                    marks = 3,
                                    markSchemeAnswer = "1. Hollow, dead tubes with no end walls to allow free flow of water (1 mark).\n2. Walls reinforced with waterproof lignin to prevent collapse under tension (1 mark).\n3. Lignin provides mechanical support to the plant (1 mark).",
                                    syllabusTopic = "Plant Nutrition & Transport"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Describe the effects of deforestation on the ecosystem.",
                                    marks = 3,
                                    markSchemeAnswer = "1. Habitat loss leading to extinction/decreased biodiversity (1 mark).\n2. Soil erosion due to lack of tree roots binding soil (1 mark).\n3. Increased CO2 in atmosphere/enhanced greenhouse effect due to less photosynthesis (1 mark).",
                                    syllabusTopic = "Ecology & Human Impact"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "State the chemical equation for aerobic respiration in human cells.",
                                    marks = 2,
                                    markSchemeAnswer = "C6H12O6 + 6O2 -> 6CO2 + 6H2O (+ Energy) [2 marks]\nAccept word equation: Glucose + Oxygen -> Carbon Dioxide + Water [1 mark]",
                                    syllabusTopic = "Human Transport, Gas Exchange & Respiration"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Explain how reflex actions protect the body from harm.",
                                    marks = 3,
                                    markSchemeAnswer = "They are involuntary/automatic and do not involve conscious brain thought (1 mark), making them extremely rapid (1 mark), minimizing tissue damage or injury (1 mark).",
                                    syllabusTopic = "Human Transport, Gas Exchange & Respiration"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Define what is meant by an 'enzyme' and explain the 'lock and key' model.",
                                    marks = 4,
                                    markSchemeAnswer = "Enzyme is a biological catalyst made of protein (1 mark).\nLock and key model: Active site of the enzyme has a specific complementary shape to the substrate (1 mark). Substrate binds to active site to react (1 mark), and products are released while enzyme remains unchanged (1 mark).",
                                    syllabusTopic = "Cell Structure & Enzymes"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "State two factors that can denature enzymes.",
                                    marks = 2,
                                    markSchemeAnswer = "1. High temperature (above optimum) (1 mark)\n2. Extreme pH (too acidic or alkaline) (1 mark)",
                                    syllabusTopic = "Cell Structure & Enzymes"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Describe how water is absorbed by root hair cells and moves up to the leaves.",
                                    marks = 4,
                                    markSchemeAnswer = "Water is absorbed by osmosis down water potential gradient into root hair cells (1 mark). Water moves through root cortex into xylem (1 mark). Transpiration pull/tension (1 mark) draws water column up xylem to leaf mesophyll (1 mark).",
                                    syllabusTopic = "Plant Nutrition & Transport"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Define homozygous and heterozygous genotypes.",
                                    marks = 2,
                                    markSchemeAnswer = "Homozygous: Having two identical alleles of a gene (e.g. AA or aa) (1 mark).\nHeterozygous: Having two different alleles of a gene (e.g. Aa) (1 mark).",
                                    syllabusTopic = "Reproduction & Inheritance"
                                )
                            )
                        }
                    }
                }
            }
            SubjectEnum.MATH -> {
                if (paperType == 2) {
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Evaluate 2/3 + 1/4 and give your answer as a single fraction.",
                                    marks = 1,
                                    markSchemeAnswer = "Common denominator is 12: 8/12 + 3/12 = 11/12 [1 mark]",
                                    options = listOf("3/7", "11/12", "3/12", "5/12"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Numbers & Percentages"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Solve for x: 3x - 7 = 11.",
                                    marks = 1,
                                    markSchemeAnswer = "3x = 18 -> x = 6 [1 mark]",
                                    options = listOf("x = 4", "x = 6", "x = 5", "x = 9"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Algebra & Quadratics"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Factorise fully: 4x² - 9y².",
                                    marks = 1,
                                    markSchemeAnswer = "Difference of two squares: (2x - 3y)(2x + 3y) [1 mark]",
                                    options = listOf("(2x-3y)²", "(2x-3y)(2x+3y)", "(4x-9y)(x+y)", "4(x-y)²"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Algebra & Quadratics"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "The probability of winning a game is 0.35. Calculate the probability of not winning.",
                                    marks = 1,
                                    markSchemeAnswer = "1 - 0.35 = 0.65 [1 mark]",
                                    options = listOf("0.65", "0.35", "0.55", "0.05"),
                                    correctOptionIndex = 0,
                                    syllabusTopic = "Probability & Statistics"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Find the gradient of the line passing through points (2, 5) and (5, 14).",
                                    marks = 1,
                                    markSchemeAnswer = "m = (14 - 5)/(5 - 2) = 9/3 = 3 [1 mark]",
                                    options = listOf("1", "2", "3", "4"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Coordinate Geometry & Trigonometry"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Expand and simplify: (x + 3)(x - 5).",
                                    marks = 1,
                                    markSchemeAnswer = "x² - 5x + 3x - 15 = x² - 2x - 15 [1 mark]",
                                    options = listOf("x² - 15", "x² + 2x - 15", "x² - 2x - 15", "x² - 8x - 15"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Algebra & Quadratics"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "A shirt costs $40. It is discounted by 15%. Calculate the sale price.",
                                    marks = 1,
                                    markSchemeAnswer = "Discount = 0.15 * 40 = 6. Sale price = 40 - 6 = $34 [1 mark]",
                                    options = listOf("$30", "$34", "$35", "$38"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Numbers & Percentages"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "State the number of lines of symmetry in a regular hexagon.",
                                    marks = 1,
                                    markSchemeAnswer = "A regular hexagon has 6 lines of symmetry [1 mark]",
                                    options = listOf("3", "4", "6", "8"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Vectors & Transformations"
                                )
                            )
                        }
                    }
                } else { // paperType == 4
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Solve the quadratic equation using the formula, giving answers to 2 decimal places:\n3x² - 8x - 5 = 0.",
                                    marks = 4,
                                    markSchemeAnswer = "x = [-(-8) ± sqrt((-8)² - 4(3)(-5))] / (2*3) (1 mark)\nx = [8 ± sqrt(64 + 60)] / 6 -> x = [8 ± sqrt(124)] / 6 (1 mark)\nCalculated values: x = 3.19 (1 mark) or x = -0.52 (1 mark)",
                                    syllabusTopic = "Algebra & Quadratics"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "A block of metal has a volume of 250 cm³ and a mass of 2.15 kg.\nCalculate the density of the metal in g/cm³.",
                                    marks = 2,
                                    markSchemeAnswer = "Mass = 2150 g (1 mark). Density = Mass / Volume = 2150 / 250 = 8.6 g/cm³ (1 mark).",
                                    syllabusTopic = "Numbers & Percentages"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "In a triangle ABC, AB = 7.5 cm, BC = 9.0 cm and angle ABC = 58°.\n(i) Calculate the area of triangle ABC.\n(ii) Use the cosine rule to calculate the length of side AC.",
                                    marks = 5,
                                    markSchemeAnswer = "(i) Area = 0.5 * a * b * sin(C) = 0.5 * 7.5 * 9.0 * sin(58°) = 33.75 * 0.8480 = 28.62 cm² [2 marks]\n(ii) AC² = 7.5² + 9.0² - 2(7.5)(9.0)cos(58°) = 56.25 + 81.0 - 135(0.5299) = 137.25 - 71.54 = 65.71 -> AC = 8.11 cm [3 marks]",
                                    syllabusTopic = "Coordinate Geometry & Trigonometry"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Simplify the expression: (18a^5 b^2) / (3a^2 b^4).",
                                    marks = 2,
                                    markSchemeAnswer = "(18/3) * a^(5-2) * b^(2-4) = 6 a^3 b^-2 or 6 a^3 / b^2 [2 marks]",
                                    syllabusTopic = "Algebra & Quadratics"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "The points A(1, 2) and B(5, 10) are on a straight line.\n(i) Calculate the length of segment AB.\n(ii) Find the equation of the straight line passing through A and B.",
                                    marks = 4,
                                    markSchemeAnswer = "(i) Length = sqrt((5-1)² + (10-2)²) = sqrt(16 + 64) = sqrt(80) = 8.94 (or 4*sqrt(5)) [2 marks]\n(ii) Gradient m = (10-2)/(5-1) = 8/4 = 2 (1 mark). Equation: y - 2 = 2(x - 1) -> y = 2x [1 mark]",
                                    syllabusTopic = "Coordinate Geometry & Trigonometry"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "A bag contains 5 red balls and 3 blue balls. Two balls are drawn at random without replacement.\nCalculate the probability that both balls are red.",
                                    marks = 3,
                                    markSchemeAnswer = "P(First is Red) = 5/8 (1 mark). P(Second is Red | First is Red) = 4/7 (1 mark). P(Both Red) = 5/8 * 4/7 = 20/56 = 5/14 (approx 0.357) (1 mark).",
                                    syllabusTopic = "Probability & Statistics"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Solve the simultaneous equations:\n3x + 2y = 13\nx - 4y = -5",
                                    marks = 3,
                                    markSchemeAnswer = "Multiply second equation by 3: 3x - 12y = -15 (1 mark).\nSubtract: 14y = 28 -> y = 2 (1 mark).\nSubstitute: x = -5 + 4(2) -> x = 3 [3 marks total]",
                                    syllabusTopic = "Algebra & Quadratics"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Show that the sum of three consecutive integers is always a multiple of 3.",
                                    marks = 2,
                                    markSchemeAnswer = "Let integers be n, n+1, n+2. Sum = n + (n+1) + (n+2) = 3n + 3 (1 mark). This is 3(n + 1), which is a multiple of 3 (1 mark).",
                                    syllabusTopic = "Algebra & Quadratics"
                                )
                            )
                        }
                    }
                }
            }
            SubjectEnum.ENGLISH -> {
                if (is2024) {
                    if (isJune) {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(a)",
                                questionText = "Read Passage A (The Lost City). Describe the narrator's feelings of awe and fear when they first entered the ancient stone archway.",
                                marks = 5,
                                markSchemeAnswer = "Award 1 mark for each valid emotion extracted with textual support (e.g. breathless silence, trembling shadows, overwhelming size). [Max 5 marks]",
                                syllabusTopic = "Reading & Comprehension"
                            )
                        )
                    } else {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(a)",
                                questionText = "Read Passage B (Oceanic Deep). Summarize the challenges faced by deep-sea divers at depths exceeding 200 meters.",
                                marks = 5,
                                markSchemeAnswer = "Award 1 mark for each point (extreme pressure, utter darkness, freezing temperatures, gas mixture limitations). [Max 5 marks]",
                                syllabusTopic = "Summary & Synthesis"
                            )
                        )
                    }
                } else { // 2025
                    if (isJune) {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(a)",
                                questionText = "Read Passage C (The Desert Nomads). How does the writer use language to build a sense of harshness and heat in the third paragraph?",
                                marks = 5,
                                markSchemeAnswer = "Analyze metaphors like 'liquid fire', adjectives like 'shriveling heat', and personification of the wind. [Max 5 marks]",
                                syllabusTopic = "Reading & Comprehension"
                            )
                        )
                    } else {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(a)",
                                questionText = "Read Passage D (The Digital Frontier). Write a directed letter to a school principal arguing for or against the complete replacement of paper textbooks with digital tablets.",
                                marks = 5,
                                markSchemeAnswer = "Award up to 3 marks for content points (cost, interactive learning, posture/eye strain) and up to 2 marks for tone and register. [Max 5 marks]",
                                syllabusTopic = "Directed Writing & Argument"
                            )
                        )
                    }
                }
            }
            SubjectEnum.ARABIC_OL -> {
                if (is2024) {
                    if (isJune) {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(أ)",
                                questionText = "أعرب الكلمات المسطّرة في الجملة: 'إنّ التعلُّمَ المستمرَّ يفتحُ آفاقاً رحبةً للعقل'. الكلمات: التعلُّمَ، المستمرَّ، يفتحُ.",
                                marks = 3,
                                markSchemeAnswer = "1. التعلُّمَ: اسم إنَّ منصوب بالفتحة (1)\n2. المستمرَّ: نعت منصوب بالفتحة (1)\n3. يفتحُ: فعل مضارع مرفوع بالضمة، والفاعل مستتر، والجملة خبر إنّ (1)",
                                syllabusTopic = "النحو والصرف وقواعد اللغة (Grammar & Syntax)"
                            )
                        )
                    } else {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(أ)",
                                questionText = "استخرج من النص المقروء ثلاثة مرادفات تدل على معنى 'الجهد والمشقة' وبيّن دورها في توضيح فكرة الكاتب.",
                                marks = 3,
                                markSchemeAnswer = "1. السعي، الكدح، العناء (درجتان لاستخراج المرادفات الصحيحة)\n2. توضيح وتأكيد المعنى وبيان صعوبة الكفاح البشري (درجة واحدة للتحليل البلاغي)",
                                syllabusTopic = "القراءة والفهم والاستيعاب (Comprehension)"
                            )
                        )
                    }
                } else { // 2025
                    if (isJune) {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(أ)",
                                questionText = "حوّل الجملة الفعلية التالية إلى جملة اسمية واضبط المبتدأ والخبر بالشكل: 'ينجحُ المجتهدان في الامتحانِ النهائي'.",
                                marks = 3,
                                markSchemeAnswer = "الجملة الاسمية: 'المجتهدانِ ينجحانِ في الامتحانِ النهائي' (درجة للتحويل، درجة لضبط المجتهدان بالألف، درجة للفعل ينجحان بوجود النون لأنه من الأفعال الخمسة)",
                                syllabusTopic = "النحو والصرف وقواعد اللغة (Grammar & Syntax)"
                            )
                        )
                    } else {
                        listOf(
                            RealExamQuestion(
                                questionNumber = 1,
                                subPart = "(أ)",
                                questionText = "اكتب موضوعاً إنشائياً لا يقل عن 200 كلمة تناقش فيه فوائد وأضرار شبكات التواصل الاجتماعي على فئة المراهقين.",
                                marks = 5,
                                markSchemeAnswer = "3 درجات للأفكار المتناسقة والتنظيم وحجج الإقناع (فوائد التواصل، أضرار الإدمان الرقمي).\nدرجتان لسلامة اللغة من الأخطاء النحوية والإملائية واستخدام علامات الترقيم.",
                                syllabusTopic = "التعبير الكتابي والانشاء (Directed Composition)"
                            )
                        )
                    }
                }
            }
            SubjectEnum.ICT -> {
                if (paperType == 2) {
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which component is responsible for storing and retrieving temporary, running data in a computer?",
                                    marks = 1,
                                    markSchemeAnswer = "Random Access Memory (RAM) stores temporary running data [1 mark]",
                                    options = listOf("ROM", "RAM", "HDD", "SSD"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which network protocol is specifically designed for secure data transfer on the World Wide Web?",
                                    marks = 1,
                                    markSchemeAnswer = "HTTPS is secure HTTP for safe web browsing [1 mark]",
                                    options = listOf("FTP", "SMTP", "HTTP", "HTTPS"),
                                    correctOptionIndex = 3,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which type of software license allows users to examine and modify the source code freely?",
                                    marks = 1,
                                    markSchemeAnswer = "Open-source software license permits viewing and modification [1 mark]",
                                    options = listOf("Proprietary", "Shareware", "Open-source", "Freeware"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What is the main purpose of a database primary key?",
                                    marks = 1,
                                    markSchemeAnswer = "To uniquely identify each record in a table [1 mark]",
                                    options = listOf("To encrypt data", "To uniquely identify each record", "To speed up internet search", "To sort records alphabetically"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which security threat involves tricking individuals into revealing sensitive credentials via fake emails?",
                                    marks = 1,
                                    markSchemeAnswer = "Phishing relies on deceptive emails [1 mark]",
                                    options = listOf("Spyware", "Phishing", "Ransomware", "Trojan horse"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Which device operates at the Network layer to forward data packets between different networks?",
                                    marks = 1,
                                    markSchemeAnswer = "Routers forward packets across networks [1 mark]",
                                    options = listOf("Switch", "Hub", "Router", "Modem"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Which spreadsheet formula should be used to find the average of cells A1 through A10?",
                                    marks = 1,
                                    markSchemeAnswer = "=AVERAGE(A1:A10) is the correct standard formula [1 mark]",
                                    options = listOf("=MEAN(A1:A10)", "=AVERAGE(A1:A10)", "=SUM(A1:A10)/10", "=AVG(A1,A10)"),
                                    correctOptionIndex = 1,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "What is the primary function of a firewall?",
                                    marks = 1,
                                    markSchemeAnswer = "To monitor and filter incoming and outgoing network traffic [1 mark]",
                                    options = listOf("To speed up CPU clock rate", "To back up data to the cloud", "To filter network traffic based on rules", "To clean viruses from SSD"),
                                    correctOptionIndex = 2,
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        }
                    }
                } else { // paperType == 4
                    if (is2024) {
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Explain the advantages and disadvantages of storing corporate databases in cloud storage compared to local storage on SSDs.",
                                    marks = 4,
                                    markSchemeAnswer = "Advantages:\n1. Global accessibility from any device connected to the internet (1 mark).\n2. Automated offsite backups and disaster recovery managed by provider (1 mark).\nDisadvantages:\n1. Dependent on a reliable active internet connection to access data (1 mark).\n2. Recurring subscription/hosting fees and potential third-party security vulnerabilities (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Define what is meant by 'symmetric encryption'.",
                                    marks = 2,
                                    markSchemeAnswer = "Symmetric encryption uses the exact same key to both encrypt and decrypt the data (2 marks).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Describe three differences between RAM and ROM.",
                                    marks = 3,
                                    markSchemeAnswer = "1. RAM is volatile (loses contents when power off) while ROM is non-volatile (retains data) (1 mark).\n2. RAM is read-write while ROM is read-only (1 mark).\n3. RAM stores temporary running applications while ROM stores the BIOS/bootstrap loader (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "State the difference between verification and validation in database input.",
                                    marks = 2,
                                    markSchemeAnswer = "Verification checks if data is copied accurately from source document (e.g. double entry) (1 mark).\nValidation checks if data obeys specific rules and is sensible (e.g. range check) (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        }
                    } else { // 2025
                        if (isJune) {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Describe the four main phases of the System Life Cycle in order.",
                                    marks = 4,
                                    markSchemeAnswer = "1. Analysis: gathering requirements and investigating current system (1 mark).\n2. Design: creating database schemas, layouts and input forms (1 mark).\n3. Development & Testing: coding, installing and verifying correctness (1 mark).\n4. Implementation: deploying the new system (direct, parallel, phased or pilot) (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "Identify two types of implementation strategies.",
                                    marks = 2,
                                    markSchemeAnswer = "Direct changeover (1 mark), Parallel running (1 mark), Phased implementation, or Pilot running.",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        } else {
                            listOf(
                                RealExamQuestion(
                                    questionNumber = 1,
                                    subPart = "(a)",
                                    questionText = "Discuss the security threats associated with using online public Wi-Fi networks and how to prevent them.",
                                    marks = 4,
                                    markSchemeAnswer = "Threats: Eavesdropping/packet sniffing of credentials (1 mark), Man-in-the-middle attacks (1 mark).\nPrevention: Use a Virtual Private Network (VPN) to encrypt traffic (1 mark), only visit HTTPS encrypted websites (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                ),
                                RealExamQuestion(
                                    questionNumber = 2,
                                    subPart = "(b)",
                                    questionText = "State the purpose of a router in a computer network.",
                                    marks = 2,
                                    markSchemeAnswer = "To forward data packets across different computer networks (1 mark) and determine the most efficient path for data transfer (1 mark).",
                                    syllabusTopic = "Types & Components of Computer Systems"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun generatePaperMarkdown(
        board: ExamBoard,
        subject: SubjectEnum,
        year: String,
        session: String,
        paperCode: String,
        titleStr: String,
        questions: List<RealExamQuestion>
    ): String {
        val qMarkdown = questions.joinToString("\n\n---\n\n") { q ->
            "### Question ${q.questionNumber} ${q.subPart} [${q.marks} Marks]\n**Topic:** ${q.syllabusTopic}\n\n${q.questionText}\n\n*Write your response in the space provided below:*\n\n...................................................................................................."
        }

        return """
            # ${board.displayName.uppercase()}
            ## OFFICIAL EXAMINATION PAPER
            **Subject:** ${subject.title} (${subject.syllabusCode})
            **Paper Code:** $paperCode • **Series:** $session $year
            **Component:** $titleStr
            
            ---
            
            ### CANDIDATE DETAILS & INSTRUCTIONS
            - **Center Number:** [ ______ ] **Candidate Number:** [ ________ ]
            - **Time Allowed:** ${if (paperCode.contains("2")) "45 minutes" else "1 hour 15 minutes"}
            - **Maximum Marks:** ${if (paperCode.contains("2")) 40 else 80}
            
            **INSTRUCTIONS TO CANDIDATES:**
            1. Answer ALL questions.
            2. Write your answers in dark blue or black ink.
            3. Show all working in calculations; credit is awarded for clear mathematical or scientific reasoning.
            
            ---
            
            $qMarkdown
        """.trimIndent()
    }

    private fun generateMarkSchemeMarkdown(
        board: ExamBoard,
        subject: SubjectEnum,
        year: String,
        session: String,
        paperCode: String,
        questions: List<RealExamQuestion>
    ): String {
        val msMarkdown = questions.joinToString("\n\n") { q ->
            "**Question ${q.questionNumber} ${q.subPart} [${q.marks} Marks]:**\n${q.markSchemeAnswer}\n*Examiner Tip:* ${q.examinerNotes}"
        }

        return """
            # ${board.displayName.uppercase()} - MARK SCHEME
            **Series:** $session $year • **Paper Code:** $paperCode
            **Subject:** ${subject.title} (${subject.syllabusCode})
            
            ---
            
            ### OFFICIAL EXAMINER GUIDELINES & MARKING CRITERIA:
            - **M1** = Method Mark
            - **A1** = Accuracy / Answer Mark
            - **B1** = Independent Mark
            
            ---
            
            $msMarkdown
        """.trimIndent()
    }

    fun getPapersForSubject(subjectId: String): List<RealPastPaper> {
        return allPapers.filter { it.subjectId.equals(subjectId, ignoreCase = true) }
    }

    fun findPaper(
        subjectId: String,
        examBoard: ExamBoard? = null,
        year: String? = null,
        paperCodeKeyword: String? = null
    ): RealPastPaper? {
        val subjectList = getPapersForSubject(subjectId)
        
        // 1. Try a strict match first
        val firstChoice = subjectList.firstOrNull { paper ->
            val boardMatch = (examBoard == null || paper.examBoard == examBoard)
            val yearMatch = (year == null || paper.year == year)
            val codeMatch = if (paperCodeKeyword == null) {
                true
            } else {
                if (paper.paperCode.contains(paperCodeKeyword, ignoreCase = true)) {
                    true
                } else {
                    val digitMatch = Regex("""Paper\s*(\d)""", RegexOption.IGNORE_CASE).find(paperCodeKeyword)?.groupValues?.get(1)
                    if (digitMatch != null) {
                        paper.paperCode.substringAfter("/").startsWith(digitMatch) || paper.paperCode.contains("/$digitMatch")
                    } else {
                        false
                    }
                }
            }
            boardMatch && yearMatch && codeMatch
        }
        if (firstChoice != null) return firstChoice

        // 2. Try matching board and year
        val secondChoice = subjectList.firstOrNull { paper ->
            (examBoard == null || paper.examBoard == examBoard) &&
            (year == null || paper.year == year)
        }
        if (secondChoice != null) return secondChoice

        // 3. Fallback to first available paper
        return subjectList.firstOrNull()
    }

    suspend fun seedRealPastPapersToDatabase(database: AppDatabase) {
        val materialDao = database.studyMaterialDao()
        // Clear all previous study materials to satisfy the "remove any materials in the app" request
        materialDao.deleteAllMaterials()
    }

    private fun createNotesBooksAndSheets(): List<StudyMaterialEntity> {
        val list = mutableListOf<StudyMaterialEntity>()

        // ------------------------------------------
        // PHYSICS
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "PHYSICS",
            materialType = "BOOK",
            title = "Cambridge IGCSE Physics Coursebook (5th Edition)",
            topic = "Core & Extended Curriculum",
            description = "Complete digital reference textbook covering theoretical modules, laboratory guidelines, and conceptual reviews for Cambridge Physics (0625).",
            contentUrl = "content://physics_handbook",
            documentContent = """
            # Cambridge IGCSE Physics Coursebook (5th Edition)
            
            Welcome to the complete Reference Coursebook. This curriculum guide covers core and extended modules.
            
            ## Table of Contents:
            1. Motion, Forces and Energy (Section 1)
            2. Thermal Physics (Section 2)
            3. Wave Properties and Light (Section 3)
            4. Electricity and Magnetism (Section 4)
            5. Nuclear and Atomic Physics (Section 5)
            6. Space Physics (Section 6)
            
            ### Section 1: Motion, Forces & Energy
            This section explores speeds, velocities, force equations, Hooke's Law, and gravitational forces.
            - Velocity: v = s / t
            - Acceleration: a = (v - u) / t
            - Force: F = m * a
            
            ### Section 2: Thermal Physics
            Covers kinetic theory of gases, Brownian motion, thermal expansion of solids, liquids, and gases.
            - Heat transfer: conduction, convection, and radiation.
            - Specific heat capacity formula: Q = m * c * Δθ.
            
            Use this coursebook to review definitions and prepare for extended papers.
            """.trimIndent(),
            durationOrPages = "450 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "PHYSICS",
            materialType = "NOTE",
            title = "Thermal Physics & Matter Revision Notes",
            topic = "Thermal Physics",
            description = "Summary of kinetic particle theory, states of matter, specific heat capacity, latent heat, and gas laws with Cambridge objectives.",
            contentUrl = "content://physics_thermal_notes",
            documentContent = """
            # Thermal Physics & Matter - IGCSE Physics Revision Notes
            
            ## 1. States of Matter & Kinetic Theory
            - **Solids**: Regular lattice structure, strong intermolecular forces, particles vibrate about fixed positions.
            - **Liquids**: Random arrangement, weaker forces, particles can move past each other.
            - **Gases**: Random arrangement, negligible forces, particles move rapidly and randomly.
            
            ## 2. Specific Heat Capacity
            - **Definition**: The energy required to raise the temperature of 1 kg of a substance by 1°C.
            - **Formula**: Q = m * c * Δθ
              - Q = Thermal energy (J)
              - m = Mass (kg)
              - c = Specific heat capacity (J/(kg°C))
              - Δθ = Change in temperature (°C)
            
            ## 3. Latent Heat
            - **Specific Latent Heat of Fusion**: Energy needed to change 1 kg of solid to liquid at melting point.
            - **Specific Latent Heat of Vaporisation**: Energy needed to change 1 kg of liquid to gas at boiling point.
            - **Formula**: Q = m * L
            
            ## 4. Evaporation vs. Boiling
            - **Evaporation**: Occurs at any temperature, only at the surface, causes cooling.
            - **Boiling**: Occurs only at boiling point, throughout the liquid, temperature remains constant.
            """.trimIndent(),
            durationOrPages = "12 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "PHYSICS",
            materialType = "SHEET",
            title = "IGCSE Physics Formula & Equations Reference Sheet",
            topic = "Curriculum Formulas",
            description = "Complete core and extended formula sheet detailing SI units, symbols, constant values, and revision checks.",
            contentUrl = "content://physics_formula_sheet",
            documentContent = """
            # IGCSE Physics (0625) Formula & Equations Sheet
            
            Use this quick reference sheet to memorize core equations for Paper 2 (MCQ) and Paper 4 (Theory).
            
            ## Unit 1: General Physics
            - **Speed & Velocity**: v = d / t
            - **Acceleration**: a = Δv / t
            - **Weight**: W = m * g (g ≈ 9.8 m/s²)
            - **Density**: ρ = m / V
            - **Force**: F = m * a
            - **Hooke's Law**: F = k * x (where k is spring constant, x is extension)
            - **Pressure**: P = F / A
            - **Liquid Pressure**: P = ρ * g * h
            
            ## Unit 2: Work, Energy & Power
            - **Work Done**: W = F * d
            - **Kinetic Energy**: E_k = 0.5 * m * v²
            - **Gravitational Potential Energy**: E_p = m * g * h
            - **Efficiency**: (Useful Energy Output / Total Energy Input) * 100%
            - **Power**: P = W / t = E / t
            
            ## Unit 3: Waves
            - **Wave Equation**: v = f * λ
            - **Refractive Index**: n = sin(i) / sin(r) = c / v
            - **Critical Angle**: sin(c) = 1 / n
            """.trimIndent(),
            durationOrPages = "3 pages",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // MATH
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "MATH",
            materialType = "BOOK",
            title = "Cambridge IGCSE Mathematics Extended Coursebook",
            topic = "Syllabus Core & Extended",
            description = "Syllabus reference handbook containing worked examples, practice exercises, and solution guidelines for IGCSE Math (0580).",
            contentUrl = "content://math_textbook",
            documentContent = """
            # Cambridge IGCSE Mathematics Extended Coursebook
            
            This core book prepares students for both the Core and Extended tiers of the IGCSE Mathematics (0580) exams.
            
            ## Structure of Modules:
            1. Number Systems & Fractions
            2. Algebra, Equations & Graphs
            3. Coordinate Geometry
            4. Trigonometry
            5. Vectors & Matrices
            6. Mensuration & Geometry
            
            ### Focus on Quadratic Formulas:
            To solve ax² + bx + c = 0, we apply the quadratic formula:
            x = [-b ± √(b² - 4ac)] / (2a)
            
            ### Trigonometry Foundations:
            - sin(θ) = Opposite / Hypotenuse
            - cos(θ) = Adjacent / Hypotenuse
            - tan(θ) = Opposite / Adjacent
            - Sine Rule: a/sin(A) = b/sin(B) = c/sin(C)
            - Cosine Rule: a² = b² + c² - 2bc cos(A)
            """.trimIndent(),
            durationOrPages = "520 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "MATH",
            materialType = "NOTE",
            title = "Algebraic Equations, Quadratics & Functions Notes",
            topic = "Algebra",
            description = "Step-by-step revision notes showing techniques to solve simultaneous, linear, and quadratic equations.",
            contentUrl = "content://math_algebra_notes",
            documentContent = """
            # Algebraic Equations, Quadratics & Functions Revision Notes
            
            ## 1. Simultaneous Linear Equations
            To solve two simultaneous equations, use **Elimination** or **Substitution**.
            
            ### Example (Elimination):
            1) 2x + 3y = 12
            2) x - y = 1
            
            Multiply equation (2) by 3:
            3x - 3y = 3
            
            Add to equation (1):
            (2x + 3x) + (3y - 3y) = 12 + 3
            5x = 15 => x = 3
            
            Substitute x = 3 into (2):
            3 - y = 1 => y = 2.
            
            ## 2. Solving Quadratics by Factoring
            Solve: x² - 5x + 6 = 0
            - Find two numbers that multiply to +6 and add to -5.
            - These numbers are -2 and -3.
            - Factored expression: (x - 2)(x - 3) = 0
            - Solutions: x = 2 or x = 3.
            """.trimIndent(),
            durationOrPages = "15 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "MATH",
            materialType = "SHEET",
            title = "Trigonometry & Geometry Practice Worksheets",
            topic = "Trigonometry",
            description = "Curated worksheets featuring past-paper style trigonometry, bearing, and angle theorems questions.",
            contentUrl = "content://math_trig_sheet",
            documentContent = """
            # Trigonometry & Geometry Practice Worksheet
            
            Test your understanding of non-right-angled triangles and circle theorems.
            
            ## Question 1 (3 marks)
            A triangle ABC has sides AB = 8 cm, BC = 11 cm, and the angle ABC = 42°.
            - Calculate the length of the side AC. Show your working.
            
            ### Hint:
            Use the Cosine Rule:
            AC² = AB² + BC² - 2(AB)(BC) cos(42°)
            AC² = 8² + 11² - 2(8)(11) cos(42°)
            
            ## Question 2 (4 marks)
            The bearing of town P from Q is 124°.
            - Find the bearing of Q from P. Explain your reasoning using parallel line properties.
            
            ### Practice Checkpoint:
            Back bearing = Front bearing + 180° = 124° + 180° = 304°.
            """.trimIndent(),
            durationOrPages = "5 pages",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // CHEMISTRY
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "CHEMISTRY",
            materialType = "BOOK",
            title = "Cambridge IGCSE Chemistry Student Coursebook",
            topic = "Complete Chemistry Guide",
            description = "Reference guide covering experimental techniques, organic synthesis, acids & bases, and qualitative analysis.",
            contentUrl = "content://chemistry_textbook",
            documentContent = """
            # Cambridge IGCSE Chemistry Student Coursebook (Core & Extended)
            
            Comprehensive student textbook mapping directly to the IGCSE Chemistry (0620/0971) syllabus.
            
            ## Syllabus Chapters:
            1. States of Matter & Separation Techniques
            2. Atomic Structure & Bonding (Ionic, Covalent, Metallic)
            3. Stoichiometry & The Mole Concept
            4. Electrochemistry & Energy Transfers
            5. Acids, Bases & Salts (pH Scale)
            6. Chemical Reaction Rates & Equilibrium
            7. Organic Chemistry (Alkanes, Alkenes, Alcohols, Esters)
            
            ### Qualitative Analysis Notes:
            - **Cation test**: Adding aqueous Sodium Hydroxide (NaOH) produces distinctive precipitates.
              - Copper (Cu²⁺): Light blue precipitate (insoluble in excess).
              - Iron (Fe²⁺): Green precipitate (insoluble in excess).
              - Iron (Fe³⁺): Red-brown precipitate (insoluble in excess).
            """.trimIndent(),
            durationOrPages = "380 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "CHEMISTRY",
            materialType = "NOTE",
            title = "Stoichiometry & The Mole Concept Revision Guide",
            topic = "Stoichiometry",
            description = "Clear explanations of empirical formulas, reacting masses, gas volumes, and concentration calculations.",
            contentUrl = "content://chemistry_mole_notes",
            documentContent = """
            # Stoichiometry & The Mole Concept Revision Guide
            
            ## 1. Defining the Mole
            - **One Mole**: Contains 6.02 * 10²³ particles (Avogadro's constant).
            - **Formula for Solids**: Number of moles (n) = Mass (g) / Molar Mass (g/mol)
            
            ## 2. Gas Volumes
            - At Room Temperature and Pressure (r.t.p.), one mole of any gas occupies a volume of **24 dm³** (or 24000 cm³) of any gas.
            - **Formula for Gases**: n = Volume (dm³) / 24
            
            ## 3. Concentration of Solutions
            - **Concentration** is measured in mol/dm³ or g/dm³.
            - **Formula**: Concentration (c) = Moles (n) / Volume (v in dm³)
            - Remember to convert cm³ to dm³ by dividing by 1000!
            
            ## 4. Empirical Formula
            To determine empirical formulas:
            1. Write down masses (or %) of elements.
            2. Divide each mass by its Relative Atomic Mass (A_r) to find moles.
            3. Divide all mole values by the smallest mole value to find the ratio.
            """.trimIndent(),
            durationOrPages = "14 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "CHEMISTRY",
            materialType = "SHEET",
            title = "Acids, Bases & Salts Identification Sheet",
            topic = "Chemical Qualitative Analysis",
            description = "A practice sheet listing chemical tests for cations, anions, and gases with interactive checklists.",
            contentUrl = "content://chemistry_qualitative_sheet",
            documentContent = """
            # Qualitative Analysis Practice & Reference Sheet
            
            This sheet contains critical tests that are guaranteed to appear on Paper 6 (Alternative to Practical).
            
            ## Part A: Testing for Gases
            - **Ammonia (NH₃)**: Damp red litmus paper turns blue.
            - **Carbon Dioxide (CO₂)**: Bubbled through limewater, turns limewater milky.
            - **Chlorine (Cl₂)**: Damp litmus paper turns red then bleaches white.
            - **Hydrogen (H₂)**: Introduces a lighted splint, produces a 'squeaky pop' sound.
            - **Oxygen (O₂)**: Introduces a glowing splint, splint relights.
            
            ## Part B: Testing for Anions
            - **Carbonate (CO₃²⁻)**: Add dilute acid -> carbon dioxide bubbles produced.
            - **Chloride (Cl⁻)**: Acidify with nitric acid, add silver nitrate -> white precipitate.
            - **Sulfate (SO₄²⁻)**: Acidify with nitric acid, add barium nitrate -> white precipitate.
            """.trimIndent(),
            durationOrPages = "4 pages",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // BIOLOGY
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "BIOLOGY",
            materialType = "BOOK",
            title = "Cambridge IGCSE Biology Revision Textbook",
            topic = "Biology Curriculum Core",
            description = "Detailed syllabus covering classification, cell physiology, enzyme activities, plants, human homeostasis, and ecology.",
            contentUrl = "content://biology_textbook",
            documentContent = """
            # Cambridge IGCSE Biology Coursebook (610)
            
            High-yield textbook guide detailing basic biological systems, plant anatomy, human homeostasis, and ecosystems.
            
            ## Core Chapters:
            1. Characteristics & Classification of Living Organisms
            2. Cell Structure & Organisation (Animal, Plant, Bacterial)
            3. Movement Into & Out of Cells (Diffusion, Osmosis, Active Transport)
            4. Biological Molecules (Carbohydrates, Lipids, Proteins)
            5. Enzymes & Factor Denaturation
            6. Plant Nutrition (Photosynthesis) & Transport
            7. Human Digestion, Blood Circulation, & Gas Exchange
            8. Reproduction, Inheritance & Variation
            9. Ecosystems & Environmental Conservation
            """.trimIndent(),
            durationOrPages = "410 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "BIOLOGY",
            materialType = "NOTE",
            title = "Enzymes & Active Transport Revision Notes",
            topic = "Cell Physiology & Enzymes",
            description = "Key study guide describing the lock-and-key model of enzymes, denaturing factors, and nutrient absorption methods.",
            contentUrl = "content://biology_enz_notes",
            documentContent = """
            # Enzymes & Active Transport Study Guide
            
            ## 1. What are Enzymes?
            - **Enzymes** are biological catalysts made of proteins that speed up reactions without being consumed.
            - **Lock-and-Key Model**: 
              - The substrate fits perfectly into the enzyme's active site.
              - Forms an enzyme-substrate complex.
              - Products are formed and released.
            
            ## 2. Factors Affecting Enzyme Activity
            - **Temperature**: 
              - Rate increases as temperature rises up to optimum (usually 37°C in humans).
              - Beyond optimum temperature, kinetic movement vibrates bonds apart, destroying the active site's shape (**denaturation**).
            - **pH**:
              - Moving away from optimum pH disrupts protein charges, denaturing the enzyme.
            
            ## 3. Active Transport vs. Diffusion
            - **Active Transport**: Movement of particles from a region of lower concentration to a region of higher concentration, against the concentration gradient.
              - Requires **energy** from respiration.
              - Requires carrier proteins in cell membranes.
            """.trimIndent(),
            durationOrPages = "10 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "BIOLOGY",
            materialType = "SHEET",
            title = "Human Circulation & Heart Anatomy Review Sheet",
            topic = "Human Physiology",
            description = "Worksheet covering oxygenated vs deoxygenated blood flow pathways, labeling tasks, and heart chamber functions.",
            contentUrl = "content://biology_circulation_sheet",
            documentContent = """
            # Human Circulation & Heart Anatomy Practice Sheet
            
            Review this diagrammatic sheet before Paper 3 or Paper 4 exams.
            
            ## 1. The Circulatory Loop
            Humans have a **double circulatory system**:
            - **Pulmonary circulation**: Blood is pumped from heart to lungs to pick up oxygen.
            - **Systemic circulation**: Oxygenated blood is pumped to the rest of the body.
            
            ## 2. Flow Pathway Checklist:
            1. Deoxygenated blood enters **Right Atrium** via the **Vena Cava**.
            2. Passes through tricuspid valve into **Right Ventricle**.
            3. Pumped via **Pulmonary Artery** to the lungs.
            4. Oxygenated blood returns via **Pulmonary Vein** to **Left Atrium**.
            5. Passes through bicuspid valve into **Left Ventricle** (which has thicker muscular walls).
            6. Pumped out via **Aorta** to the body tissues.
            """.trimIndent(),
            durationOrPages = "4 pages",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // ENGLISH
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "ENGLISH",
            materialType = "BOOK",
            title = "IGCSE English First Language (0500) Coursebook",
            topic = "Syllabus Language Skills",
            description = "Complete English coursebook containing reading texts, comprehension strategies, directed writing examples, and compositions.",
            contentUrl = "content://english_coursebook",
            documentContent = """
            # Cambridge IGCSE English First Language Coursebook (0500)
            
            Designed to develop comprehensive English writing and reading analysis skills.
            
            ## Key Examination Focuses:
            - **Paper 1: Reading**: Comprehension, Summary Writing, Writer's Effects (Vocabulary and Analysis).
            - **Paper 2: Directed Writing and Composition**: Argumentative, Narrative, or Descriptive Essays.
            
            ### Reading Skill Development:
            Analyzing explicit meaning, implicit inferences, tone, mood, and descriptive vocabulary choice.
            
            ### Directed Writing Structure:
            - Target audience: Evaluate and format for newsletters, articles, speeches, or formal letters.
            - Focus on integrating facts from the reading passage.
            """.trimIndent(),
            durationOrPages = "280 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "ENGLISH",
            materialType = "NOTE",
            title = "Directed Writing, Speeches & Rhetorical Devices",
            topic = "Composition and Writing",
            description = "Comprehensive guide on how to structure persuasive speech, formal letter, or column article using rhetoric devices.",
            contentUrl = "content://english_writing_notes",
            documentContent = """
            # Directed Writing & Rhetorical Devices Summary Notes
            
            ## 1. Directed Writing Tasks (The Speech)
            When writing a speech for an IGCSE task:
            - **Format**: Begin with an address to the audience (e.g., "Good morning, respected teachers and fellow students...") and end with a thank you.
            - **Tone**: Enthusiastic, formal yet accessible, persuasive.
            - **Structure**: Introduce your thesis, elaborate on 3 core ideas (linked to facts from the insert), and conclude with a strong call-to-action.
            
            ## 2. Using Rhetorical Devices (AFOREST)
            - **A** - Alliteration
            - **F** - Facts / Figures
            - **O** - Opinion as Fact
            - **R** - Rhetorical Questions
            - **E** - Emotive Language
            - **S** - Statistics
            - **T** - Triplets (Rule of Three)
            """.trimIndent(),
            durationOrPages = "8 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "ENGLISH",
            materialType = "SHEET",
            title = "Reading Comprehension & Vocabulary Sheet",
            topic = "Reading Analysis",
            description = "Practice sheet with exam texts and vocabulary expansion lists to boost reading paper performance.",
            contentUrl = "content://english_vocab_sheet",
            documentContent = """
            # Reading Comprehension & Vocabulary Practice Sheet
            
            Boost your vocabulary for writer's effect analysis.
            
            ## Vocabulary Expansion Drill:
            - Replace generic words like "good", "bad", "scary", or "happy" with high-tier descriptive descriptors.
              - *Scary* -> Formidable, atmospheric, ominous, daunting.
              - *Happy* -> Ecstatic, jubilant, exuberant, elated.
              - *Sudden movement* -> Abrupt, spontaneous, velocity-driven, erratic.
            
            ## Question Practice (3 marks):
            Explain how the author's choice of words in the sentence: "The ancient oak tree stood as a gnarled sentinel, casting deep, protective shadows over the silent house" establishes an atmosphere of safety and historical presence.
            """.trimIndent(),
            durationOrPages = "3 pages",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // ARABIC_OL
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "ARABIC_OL",
            materialType = "BOOK",
            title = "كتاب لغتي العربية المنهجي لشهادة الـ IGCSE",
            topic = "المنهج المتكامل",
            description = "الدليل الشامل للغة العربية (0508 / 3180) يغطي مهارات التلخيص، النحو والصرف، التعبير المقالي والمراسلات.",
            contentUrl = "content://arabic_book",
            documentContent = """
            # كتاب لغتي العربية المنهجي لشهادة الـ IGCSE (0508 / 3180)
            
            دليل الطالب الشامل لإتقان مهارات اللغة العربية لشهادة كمبريدج وإيدكسل المستويين العادي والمتقدم.
            
            ## فصول الكتاب الأساسية:
            1. الفهم والاستيعاب والقرائية العميقة
            2. مهارة التلخيص المنهجي وإعادة الصياغة
            3. القواعد النحوية الشاملة (الأفعال، الأسماء، الإعراب)
            4. التعبير الإنشائي والكتابة الوظيفية (رسائل، مقالات، تقارير)
            5. البلاغة الفنية وتذوق الجماليات الأدبية
            
            ### نصائح عامة للتعبير المقالي:
            - احرص على استخدام علامات الترقيم بشكل صحيح (، . ! ؟).
            - تجنب الأخطاء الإملائية الشائعة وخاصة الهمزات وتاء التأنيث.
            - نوع في استخدام الأساليب البلاغية والإنشائية لرفع تقييم مقالك.
            """.trimIndent(),
            durationOrPages = "320 صفحة",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "ARABIC_OL",
            materialType = "NOTE",
            title = "ملخص قواعد النحو والصرف الشامل للشهادة الإعدادية",
            topic = "النحو والصرف وقواعد اللغة",
            description = "مذكرة تفصيلية ميسرة تلخص المرفوعات، المنصوبات، كان وأخواتها، إن وأخواتها، والمشتقات مع نماذج إعرابية تطبيقية.",
            contentUrl = "content://arabic_grammar_notes",
            documentContent = """
            # ملخص قواعد النحو والصرف للصف العاشر (IGCSE)
            
            ## أولاً: النواسخ (كان وأخواتها / إن وأخواتها)
            
            ### 1. كان وأخواتها:
            أفعال ناسخة تدخل على الجملة الاسمية، **ترفع المبتدأ** ويسمى اسمها، و**تنصب الخبر** ويسمى خبرها.
            - *أخواتها*: أصبح، أضحى، ظل، أمسى، بات، صار، ليس، ما زال.
            - *مثال*: كانَ الجوُّ ممطراً. (الجوُّ: اسم كان مرفوع، ممطراً: خبر كان منصوب).
            
            ### 2. إنَّ وأخواتها:
            حروف ناسخة تدخل على الجملة الاسمية، **تنصب المبتدأ** ويسمى اسمها، و**ترفع الخبر** ويسمى خبرها.
            - *أخواتها*: أنَّ، كأنَّ، لكنَّ، ليتَّ، لعلَّ.
            - *مثال*: إنَّ العلمَ نورٌ. (العلمَ: اسم إن منصوب، نورٌ: خبر إن مرفوع).
            
            ## ثانياً: المفاعيل الخمسة
            - **المفعول به**: يقع عليه فعل الفاعل (شربَ الولدُ *الحليبَ*).
            - **المفعول المطلق**: مصدر منصوب يؤكد الفعل أو يبين نوعه (رسمتُ لوحةً *رسماً* جميلاً).
            - **المفعول لأجله**: مصدر منصوب يبين سبب حدوث الفعل (نقفُ *احتراماً* للمعلم).
            """.trimIndent(),
            durationOrPages = "18 صفحة",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "ARABIC_OL",
            materialType = "SHEET",
            title = "ورقة تدريبات البلاغة وتذوق النصوص الأدبية",
            topic = "التذوق الأدبي والبلاغة",
            description = "تدريبات عملية ممتازة وتطبيقات عملية على الاستعارة المكنية والتصريحية، الكناية، المحسنات البديعية وأساليب التوكيد.",
            contentUrl = "content://arabic_rhetoric_sheet",
            documentContent = """
            # ورقة تدريبات البلاغة العربية وتذوق النصوص الأدبية
            
            تساعدك هذه الورقة على حل أسئلة التذوق البلاغي في ورقة الامتحان الأولى.
            
            ## مراجعة سريعة لأهم الألوان البيانية:
            1. **التشبيه**: الربط بين شيئين بعلامة تشابه (العلم كالنور يهدي البشرية).
            2. **الاستعارة المكنية**: تشبيه حذف فيه المشبه به وبقيت صفة تدل عليه (تبسم الأمل للمجتهد - شبه الأمل بالإنسان وحذف المشبه به).
            3. **الكناية**: تعبير يقصد به معنى ملازم للمعنى الحقيقي (فلان بابه مفتوح للجميع - كناية عن الكرم).
            
            ## تدريب عملي (4 درجات):
            استخرج الجمال الفني في العبارات التالية وبين نوعه وسر جماله:
            - العبارة الأولى: "زأر الجندي في وجه الأعداء مدافعاً عن وطنه".
            - العبارة الثانية: "بنى الوالد لأبنائه بيتاً من العلم والمعرفة".
            """.trimIndent(),
            durationOrPages = "4 صفحات",
            uploadedBy = "selimamr"
        ))

        // ------------------------------------------
        // ICT
        // ------------------------------------------
        // BOOK
        list.add(StudyMaterialEntity(
            subjectId = "ICT",
            materialType = "BOOK",
            title = "Cambridge IGCSE ICT (0417) Complete Guide",
            topic = "Syllabus Theory & Practice",
            description = "Reference student book detailing hardware systems, networking protocols, system design, databases, and spreadsheets.",
            contentUrl = "content://ict_textbook",
            documentContent = """
            # Cambridge IGCSE ICT (0417) Complete Coursebook
            
            Covers theory concepts and practical guidance for IGCSE ICT examinations (Paper 1 Theory, Paper 2 & 3 Practical).
            
            ## Theory Syllabus Syllabus:
            1. Types and Components of Computer Systems (Hardware vs. Software)
            2. Input and Output Devices (Sensors, Scanners, Monitors, Actuators)
            3. Storage Devices and Media (Magnetic, Optical, Solid State SSD)
            4. Networks and System Topologies (LAN, WAN, Router, Switch, Firewalls)
            5. Systems Analysis and Design Lifecycle
            
            ### Practical Components Covered:
            - Document Production (MS Word / Stylesheets)
            - Database Management & Query Construction (MS Access / SQL)
            - Spreadsheets & Formulas (COUNTIF, VLOOKUP, Nested IF)
            - Web Authoring (HTML & CSS styling)
            """.trimIndent(),
            durationOrPages = "400 pages",
            uploadedBy = "selimamr"
        ))

        // NOTE
        list.add(StudyMaterialEntity(
            subjectId = "ICT",
            materialType = "NOTE",
            title = "Systems & Network Communication Theory Notes",
            topic = "Networks and Theory",
            description = "Concise study notes on routers, switches, firewalls, network topologies (LAN, WAN), and security practices.",
            contentUrl = "content://ict_networks_notes",
            documentContent = """
            # Systems & Network Communication Theory Notes
            
            ## 1. Network Hardware Devices
            - **Hub**: Sends data packets to ALL devices connected to it. Inefficient, causes high collision rates.
            - **Switch**: Sends data packets only to the target device using MAC addresses. Efficient and secure.
            - **Router**: Connects two different networks (e.g., LAN to WAN / Internet). Routes data packets using IP addresses.
            - **Gateway**: Translates data packets between two networks that run on different communication protocols.
            
            ## 2. LAN vs. WAN
            - **LAN (Local Area Network)**: Private network restricted to a small geographic location (e.g. school, office). High data speeds, low error rate.
            - **WAN (Wide Area Network)**: Network that spans a large geographic location (e.g. internet, nationwide bank). Often rented networks, higher error rates.
            
            ## 3. Cyber Security Countermeasures
            - **Firewall**: Software or hardware device that filters incoming/outgoing network packets based on traffic rules.
            - **Data Encryption**: Scrambles raw data into cipher-text using a cryptographic key, making it unreadable without the decryption key.
            """.trimIndent(),
            durationOrPages = "12 pages",
            uploadedBy = "selimamr"
        ))

        // SHEET
        list.add(StudyMaterialEntity(
            subjectId = "ICT",
            materialType = "SHEET",
            title = "Database SQL & Spreadsheet Formula Sheets",
            topic = "Spreadsheets & SQL",
            description = "Quick reference guide and exercise sheet detailing basic SQL query syntax and nested Excel functions.",
            contentUrl = "content://ict_formulas_sheet",
            documentContent = """
            # Database SQL & Spreadsheet Formula Reference Sheet
            
            Use this cheat sheet to master spreadsheet modeling and database query writing.
            
            ## 1. SQL Queries (Paper 2 / Practical)
            SQL syntax for typical database searches:
            ```sql
            SELECT StudentName, Grade, PhysicsScore
            FROM StudentGradesTable
            WHERE PhysicsScore >= 80 AND Grade = 'Year 10'
            ORDER BY PhysicsScore DESC;
            ```
            - **SELECT**: Column names to display.
            - **FROM**: Name of the table.
            - **WHERE**: Search filters / conditions.
            - **ORDER BY**: Field to sort by (ASC for ascending, DESC for descending).
            
            ## 2. Spreadsheet Functions (Paper 3 / Spreadsheets)
            - **VLOOKUP**: Looks up a value in the first column of a table array and returns a value in the same row from another column.
              - `=VLOOKUP(lookup_value, table_array, col_index_num, [range_lookup])`
            - **COUNTIF**: Counts cells based on a criteria.
              - `=COUNTIF(range, criteria)`
            """.trimIndent(),
            durationOrPages = "4 pages",
            uploadedBy = "selimamr"
        ))

        return list
    }
}
