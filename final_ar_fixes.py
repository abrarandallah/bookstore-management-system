# Consolidated, idempotent fix script - safe to run no matter what earlier
# rounds already applied, since every value here is a final target, not a
# relative edit. Only touches messages_ar.properties.

ar_fixes = {
    # === Round 1 (HTML entities handled separately - see note below) ===
    "admin_users.email": "البريد الإلكتروني",
    "bookEdit.edit.book": "تعديل الكتاب",
    "bookEdit.1.ndash.10.short.pages": "من 1 إلى 10 صفحات قصيرة، كل صفحة تحتوي على عنوان والفكرة نفسها.",
    "bookEdit.takeaway": "خلاصة",
    "bookEdit.submit": "إرسال",
    "bookList.title.a.ndash.z": "العنوان (A–Z)",
    "bookList.author.a.ndash.z": "المؤلف (A–Z)",
    "bookRegister.bulk.import": "استيراد جماعي",
    "bookRegister.1.ndash.10.short.pages": "من 1 إلى 10 صفحات قصيرة، كل صفحة تحتوي على عنوان والفكرة نفسها.",
    "bookRegister.takeaway": "خلاصة",
    "error.oops": "عذرًا",
    "fragments_layout.fran.ais": "الفرنسية",
    "fragments_layout.bookstore": "BookStore",
    "fragments_layout.login": "تسجيل الدخول",
    "fragments_layout.avatar": "الصورة الشخصية",
    "genres.browse": "تصفح",
    "genres.manage.genres": "إدارة الأنواع",
    "home.login": "تسجيل الدخول",
    "login.login": "تسجيل الدخول",
    "login.resend.verification.email": "إعادة إرسال بريد التحقق",
    "privacy.legal": "الشؤون القانونية",
    "privacy.no.third.party.analytics.or": "لا توجد أدوات تحليل أو تتبع من أطراف خارجية",
    "profile.profile": "الملف الشخصي",
    "profile.voracious.reader": "🔥 قارئ نهم",
    "profile.bookworm": "🐛 عاشق الكتب",
    "profile.avatar": "الصورة الشخصية",
    "register.email": "البريد الإلكتروني",
    "resendVerification.resend.verification.email": "إعادة إرسال بريد التحقق",
    "resetPasswordInvalid.reset.link.invalid": "رابط إعادة التعيين غير صالح",
    "resetPasswordInvalid.reset.link.invalid.1": "رابط إعادة التعيين غير صالح",
    "settings.fran.ais": "الفرنسية",
    "terms.legal": "الشؤون القانونية",
    "terms.a.reasonable.generic.starting.point": "نقطة انطلاق عامة معقولة، وليست بديلاً عن استشارة قانونية حقيقية - على من يدير هذا الموقع مراجعتها قبل الاعتماد عليها في نشر حقيقي بمستخدمين حقيقيين.",
    "genres.pick.a.genre.to.see": "اختر نوعًا لترى الكتب المصنّفة تحته.",
    "genres.no.genres.yet": "لا توجد أنواع بعد.",
    "genres.fix.a.typo.or.merge": "أصلح خطأً إملائيًا، أو ادمج نسخة مكررة في نوع موجود.",
    "genres.save": "احفظ",
    "genres.merge": "دمج",
    "genres.only.genre": "النوع الوحيد",
    "fragments_layout.home": "الرئيسية",
    "fragments_layout.register": "إنشاء حساب",
    "settings.english": "الإنجليزية",
    "bookList.all.genres": "جميع الأنواع",
    "login.don.t.have.an.account": "ليس لديك حساب بعد؟ أنشئ واحدًا هنا",
    "login.account.created.successfully.check.your": "تم إنشاء الحساب بنجاح. تحقق من بريدك الإلكتروني للحصول على رابط التحقق قبل تسجيل الدخول.",
    "admin_import.each.book.needs.1.ndash": "يحتاج كل كتاب إلى 1 إلى 10 خلاصات. الصفوف التي تفشل في التحقق يتم تخطيها وسردها أعلاه - كل شيء آخر لا يزال يُستورد.",
    "bookRegister.each.book.needs.1.ndash": "يحتاج كل كتاب إلى 1 إلى 10 خلاصات. الصفوف التي تفشل في التحقق يتم تخطيها وسردها أعلاه - كل شيء آخر لا يزال يُستورد.",
    "bookList.search.by.title.or.author": "البحث عن طريق العنوان أو المؤلف…",

    # === Round 2 ===
    "about.about.bookstore": "حول BookStore",
    "home.about.bookstore": "حول BookStore",
    "home.most.books.have.a.handful": "معظم الكتب لها حفنة من الأفكار الجديرة بالتذكر، ملفوفة في صفحات القصص والتكرار. يختصر BookStore كل كتاب إلى بضع خلاصات قصيرة يمكنك قراءتها في دقائق، لا ساعات.",
    "bookRegister.register.a.book": "سجّل كتابًا",
    "bookRegister.add.books": "إضافة الكتب",
    "bookRegister.register.one.book.with.its": "سجّل كتابًا واحدًا بخلاصاته، أو الصق مصفوفة JSON لاستيراد عدة كتب دفعة واحدة.",
    "bookRegister.remove": "إزالة",
    "bookRegister.submit": "إرسال",
    "bookRegister.paste.a.json.array.of": "الصق مصفوفة JSON من الكتب - أسرع من ملء النموذج مرة تلو الأخرى.",
    "bookRegister.books.json.array": "الكتب (مصفوفة JSON)",
    "bookRegister.import": "استيراد",
    "bookRegister.heading.e.g.start.before": "العنوان، مثلاً 'ابدأ قبل أن تكون جاهزًا'",

    # === Round 3: grammar fixes on placeholders you flagged (word choice, not just punctuation) ===
    "bookEdit.what.s.the.key.idea": "ما هي الفكرة الرئيسية؟",
    "bookRegister.what.s.the.key.idea": "ما هي الفكرة الرئيسية، في بضع جمل؟",
    "changePassword.don.t.remember.your.current": "ألا تتذكر كلمة سرك الحالية؟",
    "login.forgot.password": "نسيت كلمة السر؟",

    # === Round 3: duplicate-key inconsistency fixes (same EN text, was getting different/wrong AR per key) ===
    "admin_users.manage.users": "إدارة المستخدمين",
    "bookEdit.genres": "الأنواع",
    "genres.genres": "الأنواع",
    "forgotPassword.username.or.email": "اسم المستخدم أو البريد الإلكتروني",
    "resendVerification.username.or.email": "اسم المستخدم أو البريد الإلكتروني",
    "register.register": "إنشاء حساب",
    "privacy.settings": "الإعدادات",
    "settings.password": "كلمة المرور",
    "readingHistory.reading.history": "سجل القراءة",

    # === Round 3: admin/users page - new bugs found today ===
    "admin_users.change.role": "تغيير الدور",
    "admin_users.delete": "حذف",
    "admin_users.reader": "قارئ",
    "admin_users.save": "حفظ",
    "admin_users.reset": "إعادة تعيين",
    "admin_users.promote.readers.to.librarian.or": "رقّ القراء إلى أمين مكتبة، أو أعد تعيين كلمة سر أي شخص.",

    # === Round 3: the "spine" tagline - not translatable literally, rewritten to keep the same repetition device ===
    "fragments_layout.tagline": "— لكل كتاب حكاية، ولكل حكاية ما تحكيه لك.",

    # === Round 3, second pass: more duplicate-key inconsistencies found after re-checking ===
    "admin_import.paste.a.json.array.of": "الصق مصفوفة JSON من الكتب - أسرع من ملء النموذج مرة تلو الأخرى.",
    "admin_import.books.json.array": "الكتب (مصفوفة JSON)",
    "admin_import.import": "استيراد",
    "genres.save": "حفظ",
    "bookEdit.remove": "إزالة",
    "myBooks.remove": "إزالة",
    "fragments_layout.add.books": "إضافة الكتب",
    "fragments_layout.english": "الإنجليزية",
    "home.home": "الرئيسية",
}

def apply_fixes(path, fixes):
    with open(path, encoding='utf-8') as f:
        lines = f.readlines()
    applied = set()
    new_lines = []
    for line in lines:
        if '=' in line and not line.strip().startswith('#'):
            k, v = line.split('=', 1)
            k = k.strip()
            if k in fixes:
                new_lines.append(f"{k}={fixes[k]}\n")
                applied.add(k)
                continue
        new_lines.append(line)
    with open(path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
    missing = set(fixes) - applied
    return applied, missing

applied, missing = apply_fixes('src/main/resources/messages_ar.properties', ar_fixes)
print(f"Applied: {len(applied)} fixes to messages_ar.properties")
if missing:
    print(f"WARNING - keys not found (check key names, maybe already renamed): {missing}")

# Also make sure the HTML-entity fix from round 1 is applied, idempotently
import re
def entity_fix(text):
    return (text.replace('&ndash;', '–').replace('&mdash;', '—')
                .replace('&hellip;', '…').replace('&middot;', '·'))
for path in ['src/main/resources/messages.properties',
             'src/main/resources/messages_fr.properties',
             'src/main/resources/messages_ar.properties']:
    with open(path, encoding='utf-8') as f:
        lines = f.readlines()
    new_lines = []
    for line in lines:
        if '=' in line and not line.strip().startswith('#'):
            k, v = line.split('=', 1)
            v = entity_fix(v)
            v = re.sub(r'(\w)ndash;', r'\1–', v)
            v = re.sub(r'(\w)mdash;', r'\1—', v)
            v = re.sub(r'(\w)hellip;', r'\1…', v)
            new_lines.append(k + '=' + v)
        else:
            new_lines.append(line)
    with open(path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
print("HTML entity fix re-applied (idempotent) to all three files.")

# The tagline exists in FR/EN too but wasn't broken there - just confirming it's present