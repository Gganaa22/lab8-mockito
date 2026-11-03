# Лаб №8: Mock объектууд — Mockito

**Оюутан:** Д.Гантогтох  
**Код:** B242270139

## Орчин

`java -version`:

java version “25.0.2” 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 25.0.2+10-LTS-69)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.2+10-LTS-69, mixed mode, sharing)


`mvn -version`:

Apache Maven 3.9.12 (848fbb4bf2d427b72bdb2471c22fced7ebd9a7a1)
Maven home: C:\Program Files\apache-maven-3.9.12
Java version: 25.0.2, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-25.0.2
Default locale: en_US, platform encoding: UTF-8
OS name: “windows 11”, version: “10.0”, arch: “amd64”, family: “windows”


## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, BUILD SUCCESS.
(7 энгийн тест + A8-ийн параметржүүлсэн 3 мөр.)

## A8 дахь lenient()

A8-ийн мөр бүр `register()`-ийн өөр өөр хэсэгт зогсдог тул stub-уудын заримыг ашиглахгүй (жишээ нь ERROR_NO_STUDENT мөрөнд `courseExists` хэзээ ч дуудагдахгүй). MockitoExtension-ий strict stubs горим ашиглагдаагүй stub-ийг `UnnecessaryStubbingException` гэж унагаадаг тул нийтлэг stub-уудыг `lenient().when(...)`-оор зарласан.

## Хэсэг B: мини мутаци

| Мутант | Эвдрэл | Унасан тестүүд | Failures/Errors | Файл |
|---|---|---|---|---|
| 1 | Идэвхгүй оюутны шалгалтыг комментлосон | `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail` (A3), `register_errorCases_returnExpectedCode[2]` (A8) | 2 / 0 | `results/mvn-test-mutant-1.txt` |
| 2 | `saveRegistration(...)` дуудалтыг комментлосон | `register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail` (A1), `register_emailSenderThrows_stillReturnsOkAndSaves` (A5) | 2 / 0 | `results/mvn-test-mutant-2.txt` |
| 3 | `\|\| courseId == null` хэсгийг устгасан | `register_nullArguments_throwsIllegalArgumentAndNoInteractions` (A6) | 1 / 0 | `results/mvn-test-mutant-3.txt` |

Бүх мутант BUILD FAILURE өгсөн, UnnecessaryStubbingException гараагүй. Мутант 3-ыг A6 эхний оролдлогоор барьсан, учир нь A6-г анхнаасаа `studentId` ба `courseId` аргумент тус бүрийн `null`-ийг тусад нь шалгадаг (хоёр `assertThrows`) байдлаар бичсэн. Тиймээс нэмэлт засвар шаардлагагүй байв. Мутант бүрийн дараа код эх хэлбэрээрээ сэргээгдэж, эцсийн `results/mvn-test.txt` ногоон байна.

## Дүгнэлт

Mock нь дуудалтыг бүртгэж, урьдчилан тохируулсан хариуг буцааж, "ямар метод хэдэн удаа дуудагдав" гэдгийг verify хийх боломж олгодог. Fake нь гараар бичсэн, жинхэнэ ажилладаг хөнгөн хэрэгжүүлэлт (жишээ нь HashMap-аар хэрэгжсэн repository) бөгөөд үр дүнгийн төлөвийг шалгахад тохиромжтой. Mock нь хурдан тохируулагддаг ч тестийг дуудалтын дэлгэрэнгүйтэй холбодог, fake нь илүү бодит боловч өөрөө засвар шаарддаг. Өөрийн Student шиг энгийн өгөгдлийн объект, value төрөл, коллекц, мөн тестлэж буй классаа өөрийг нь mock хийх ёсгүй, учир нь тэгвэл бодит логикийг биш mock-ийн тохиргоог тестэлнэ. Мөн эзэмшдэггүй гуравдагч талын сангийн төрлийг шууд mock хийхээс зайлсхийж, өөрийн нимгэн интерфейсээр ороох нь дээр. verify-г хэтрүүлбэл (дуудалтын тоо, дараалал, бүх аргументыг шалгах) тест хэрэгжүүлэлтийн дотоод бүтэцтэй нягт уягдаж, зөв рефакторинг хийхэд ч тест унадаг. Ийм brittle тест нь үнэн хэрэгтээ зан төлөвийг биш кодын хэлбэрийг шалгаж, засварын зардлыг нэмнэ. Тиймээс гол үр дүнг assert-ээр, зөвхөн чухал гадаад нөлөөг (хадгалсан, и-мэйл явуулсан) verify-аар шалгах нь зөв тэнцвэр юм.