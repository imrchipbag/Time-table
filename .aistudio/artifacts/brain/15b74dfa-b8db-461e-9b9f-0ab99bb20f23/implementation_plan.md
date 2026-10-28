# Photo Schedule Extraction via Gemini 3.8 Flash

Enable users to effortlessly upload or snap a photo of their timetable (printed, handwritten, or digital) and automatically extract all classes, clinical postings, and practical labs with days and times using Gemini 3.8 Flash, followed by an interactive review before applying.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and guide this implementation:

- **Confirmed Decision 1 (Photo Capture Source)**: Provide options to **either choose an existing photo from the device gallery or snap a live photo with the camera**.
- **Confirmed Decision 2 (Schedule Review Flow)**: Extracted sessions will be displayed in an **interactive editable list** where users can inspect, fine-tune, or correct the course name, session type (CLASS, POSTING, LAB), day of the week, and start/end times before saving.
- **Confirmed Decision 3 (Model Selection)**: Use **Gemini 3.8 Flash** (`gemini-3.8-flash`) via multimodal inference (`content { image(...); text(...) }`) for fast, accurate extraction of complex timetable tables, headings, and abbreviations.
- **Open Choice with Recommended Default**: Fallback to manual schedule entry remains available so users are never blocked if a photo is blurry.

---

## 1. Overview & Core Concept

- **What It Does**: Adds a dedicated "Scan / Photo" mode to the Schedule Setup dialog. Users select an image from their gallery or capture a photo with their camera. Gemini 3.8 Flash analyzes the image, identifies timetable columns/rows, determines whether each item is a theory lecture, clinical posting, or practical lab, resolves start/end times, and populates the editable session list.
- **Target Audience / Persona**: Students and medical trainees juggling rotating postings, labs, and classroom lectures who want to digitize paper or spreadsheet timetables without manual typing.
- **Key Value**: Drastically reduces initial setup friction from minutes of manual data entry to a single photo capture with instant, structured verification.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Initiate Scan**:
   - In the **Timetable** tab, tapping **"Set Schedule"** opens the Schedule Setup modal.
   - At the top of the modal, a toggle or action button offers: **"Photo / Scan"** alongside **"Manual Entry"**.
2. **Select or Capture Photo**:
   - Two clear action buttons:
     - **"Camera"** (takes a photo using the system camera contract).
     - **"Gallery"** (launches the zero-permission photo picker `ActivityResultContracts.PickVisualMedia`).
3. **AI Extraction State**:
   - While Gemini 3.8 Flash processes the image, a subtle loading indicator displays:
     *"Gemini 3.8 Flash is analyzing timetable, identifying classes, postings & labs..."*
   - Includes a cancel button and image thumbnail preview.
4. **Interactive Review**:
   - Once extracted, the sessions smoothly load into the editable list.
   - Each item clearly displays its detected session badge: `POSTING`, `LAB`, or `CLASS`.
   - Days and start/end time pickers are editable so the user can correct any ambiguities.
5. **Save & Apply**:
   - The user selects the term duration (Start Date to Applicable Until Date) and taps **"Apply Schedule"**.
   - Timetable instantly populates, streak calculations calibrate, and class reminders schedule.

### Visual Identity & Theme

- **Color Tokens**:
  - Dark minimalist background: `MonoBlack` (`#000000`) and elevated cards `MonoSurfaceCard` (`#141414`).
  - Active borders and high-contrast accents: `MonoWhite` and `MonoBorderBright`.
  - Type badges: Distinct monochrome pills for `CLASS`, `POSTING`, `LAB`.
- **Typography**: Clean, bold headings matching Tally's distinctive typographic style (`FontWeight.Black`, clean letter spacing).
- **Feedback**: Non-blocking toast notifications on successful schedule extraction or error recovery.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Multimodal Gemini 3.8 Flash vs. Local ML Kit OCR**
  - *Chosen Approach*: Gemini 3.8 Flash multimodal API with structured prompt instructions.
  - *Why*: Complex academic and medical timetables have merged cells, non-standard abbreviations (e.g. *MED302*, *SURG Posting*, *Biochem Lab*), and multi-row headers that raw OCR cannot semantically reconstruct into time intervals and session types.
  - *Alternatives Considered*: Basic on-device text recognition (failed to identify relationships between time headers and course cells).

- **Decision 2: Image Compression Prior to API Transmission**
  - *Chosen Approach*: Downscale high-resolution camera images to a maximum dimension of 1600px and compress to JPEG under 2MB before sending.
  - *Why*: Reduces network upload latency from 8+ seconds to under 1 second while preserving crisp readability for text extraction.

- **Decision 3: Zero Mock Data Preservation**
  - *Chosen Approach*: The schedule review list only contains items actually detected in the user's photo. If no sessions are found, an explicit warning is shown rather than fallback fake data.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌───────────────────────────────────────────────────────────────┐
│                    UploadTimetableDialog                      │
│                                                               │
│   ┌───────────────────────┐       ┌───────────────────────┐   │
│   │   Gallery Picker      │       │     Camera Capture    │   │
│   │ (PickVisualMedia)     │       │ (TakePicturePreview)  │   │
│   └──────────┬────────────┘       └───────────┬───────────┘   │
│              │                                │               │
│              └───────────────┬────────────────┘               │
│                              ▼                                │
│               Bitmap Scaling & Optimization                   │
│                              │                                │
│                              ▼                                │
│               GeminiTimetableExtractor                        │
│               (Gemini 3.8 Flash Multimodal)                   │
│                              │                                │
│                              ▼                                │
│                ParsedCourseSlot JSON Objects                  │
│                              │                                │
│                              ▼                                │
│               Interactive EditableSlotCards                   │
│         [Type: POSTING/CLASS/LAB | Day | Times]               │
│                              │                                │
│                              ▼                                │
│                    AttendanceRepository                       │
│           (Room Database: Courses & TimeSlots)                │
└───────────────────────────────────────────────────────────────┘
```

### Component & Data Mapping

1. **`GeminiTimetableExtractor`**:
   - Multimodal helper passing optimized Bitmap + tailored prompt to `gemini-3.8-flash`.
   - Formulates prompt requiring strict JSON array with `courseName`, `courseCode`, `dayOfWeek` (1-7), `startTime` ("HH:mm"), `endTime` ("HH:mm"), and `sessionType` ("CLASS", "POSTING", "LAB").
   - Catches timeouts and surfaces actionable errors.

2. **`UploadTimetableDialog`**:
   - Integrates `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())` and `rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview())`.
   - Displays live extraction status and error banners.
   - Populates `editableSlots` with parsed items for user fine-tuning.

3. **`AttendanceRepository`**:
   - Implements `importParsedSlots` to persist confirmed courses and time slots into Room SQLite database.
