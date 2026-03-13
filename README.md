# 📁 File Organizer Tool 🚀

[![Java](https://img.shields.io/badge/Java-17+-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ Features
- **🔍 Auto-detects** file types by extension (Images, Videos, PDFs, Documents, Audio, Others)
- **📅 Organizes by year** (based on modification date) inside category folders
- **✅ Dry-run mode** to preview changes before actual moves
- **🔄 Handles duplicates** automatically (file(1).jpg, file(2).jpg, etc.)
- **📝 Complete logging** to `organized_files_log.txt`
- **⚡ Professional OOP** design with clean code & detailed comments
- **🚀 Production-ready** - Error handling & summary statistics

## 🛠️ Quick Start
```bash
# Compile
javac src/main/java/com/fileorganizer/FileOrganizer.java

# Dry-run (preview changes - RECOMMENDED first)
java com.fileorganizer.FileOrganizer "C:\\Users\\YourName\\Downloads" true

# Real organize
java com.fileorganizer.FileOrganizer "C:\\Users\\YourName\\Downloads" false
```

## 📂 Folder Structure After Organization
```
YourFolder/
├── Images/
│   ├── 2023/
│   └── 2024/
├── Videos/
│   ├── 2023/
│   └── 2024/
├── PDFs/
├── Documents/
├── Audio/
├── Others/
└── organized_files_log.txt 📋
```

## 🎯 File Categories & Extensions
| Category | Extensions |
|----------|------------|
| 🖼️ Images | jpg, jpeg, png, gif, bmp, tiff |
| 🎥 Videos | mp4, avi, mkv, mov, wmv |
| 📄 PDFs | pdf |
| 📝 Documents | doc, docx, xls, xlsx, ppt, pptx, txt |
| 🎵 Audio | mp3, wav, flac, aac |
| 📦 Others | everything else |

## 📊 Example Output
```
*** DRY-RUN MODE ENABLED *** (No files will be moved)
Starting file organization for: C:\Users\TheProper\Downloads
Mode: DRY-RUN

[DRY RUN] Would move: photo.jpg -> Images/2024/photo.jpg
[DRY RUN] Would move: document.pdf -> PDFs/2024/document.pdf
[DRY RUN] Would move: song.mp3 -> Audio/2023/song.mp3

=== SUMMARY ===
Files scanned: 156
Files moved: 142
Files skipped: 14
Log saved to: C:\Users\TheProper\Downloads\organized_files_log.txt
```

## 🧑‍💻 For Developers / IDE Users
1. **IntelliJ/Eclipse**: Open `FileOrganizer` folder as project
2. **Right-click** `FileOrganizer.java` → Run with arguments: `"C:\\path\\to\\folder" true`
3. Extend easily:
   - Add new extensions in `FILE_CATEGORIES` HashMap
   - Modify `getYearFolder()` for different date logic

## 📈 Perfect for BCA CV / Portfolio
- **Clean, professional code** with OOP principles
- **Real-world utility** tool
- **Comprehensive features** with error handling
- **GitHub-ready** with badges & documentation

## 🤝 License
MIT License - Feel free to use in your projects!

---

⭐ **Star this repo if it helped you!** ⭐

