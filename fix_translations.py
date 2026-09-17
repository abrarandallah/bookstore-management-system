import re

def entity_fix(text):
    return (text.replace('&ndash;', '–')
                .replace('&mdash;', '—')
                .replace('&hellip;', '…')
                .replace('&middot;', '·'))

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
print("Entity fix pass done.")

ar_fixes = {
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
    "bookRegister.submit": "إرسال",
    "error.oops": "عذرًا",
    "fragments_layout.fran.ais": "الفرنسية",
    "fragments_layout.bookstore": "BookStore",
    "fragments_layout.login": "تسجيل الدخول",
    "fragments_layout.avatar": "الصورة الشخصية",
    "genres.browse": "تصفح",
    "genres.manage.genres": "إدارة الأصناف",
    "genres.genre": "الصنف",
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
    "genres.genres": "الأصناف",
    "genres.pick.a.genre.to.see": "اختر صنفًا لترى الكتب المصنّفة تحته.",
    "genres.no.genres.yet": "لا توجد أصناف بعد.",
    "genres.fix.a.typo.or.merge": "أصلح خطأً إملائيًا، أو ادمج نسخة مكررة في صنف موجود.",
    "genres.save": "احفظ",
    "genres.merge": "دمج",
    "genres.only.genre": "الصنف الوحيد",
    "fragments_layout.home": "الرئيسية",
    "fragments_layout.register": "إنشاء حساب",
    "settings.english": "الإنجليزية",
    "bookList.all.genres": "جميع الأصناف",
    "login.don.t.have.an.account": "ليس لديك حساب بعد؟ أنشئ واحدًا هنا",
    "login.account.created.successfully.check.your": "تم إنشاء الحساب بنجاح. تحقق من بريدك الإلكتروني للحصول على رابط التحقق قبل تسجيل الدخول.",
    "admin_import.each.book.needs.1.ndash": "يحتاج كل كتاب إلى 1 إلى 10 خلاصات. الصفوف التي تفشل في التحقق يتم تخطيها وسردها أعلاه - كل شيء آخر لا يزال يُستورد.",
    "bookRegister.each.book.needs.1.ndash": "يحتاج كل كتاب إلى 1 إلى 10 خلاصات. الصفوف التي تفشل في التحقق يتم تخطيها وسردها أعلاه - كل شيء آخر لا يزال يُستورد.",
    "bookList.search.by.title.or.author": "البحث عن طريق العنوان أو المؤلف…",
}

punct_fix_keys = [
    "bookEdit.what.s.the.key.idea",
    "bookRegister.what.s.the.key.idea",
    "changePassword.don.t.remember.your.current",
    "login.forgot.password",
]

fr_fixes = {
    "bookShare.this.book.hasn.t.been": "Ce livre n'a pas encore été résumé.",
    "bookShare.create.a.free.account": "Créer un compte gratuit",
    "bookShare.cover": "Couverture",
    "changePassword.change.password": "Changer le mot de passe",
    "fragments_layout.english": "Anglais",
    "settings.english": "Anglais",
    "fragments_layout.home": "Accueil",
    "fragments_layout.register": "S'inscrire",
    "register.register": "Inscription",
    "forgotPassword.send.reset.link": "Envoyer le lien de réinitialisation",
    "bookList.search.by.title.or.author": "Recherche par titre ou auteur…",
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

ar_applied, ar_missing = apply_fixes('src/main/resources/messages_ar.properties', ar_fixes)
fr_applied, fr_missing = apply_fixes('src/main/resources/messages_fr.properties', fr_fixes)
print(f"AR fixes applied: {len(ar_applied)}, missing: {ar_missing}")
print(f"FR fixes applied: {len(fr_applied)}, missing: {fr_missing}")

with open('src/main/resources/messages_ar.properties', encoding='utf-8') as f:
    lines = f.readlines()
new_lines = []
for line in lines:
    if '=' in line and not line.strip().startswith('#'):
        k, v = line.split('=', 1)
        k = k.strip()
        v = v.rstrip('\n')
        if k in punct_fix_keys:
            v = v.rstrip()
            for suffix in ['؟?', '?؟', ';؛', '؛;']:
                if v.endswith(suffix):
                    v = v[:-1]
                    break
            new_lines.append(f"{k}={v}\n")
        else:
            new_lines.append(line)
    else:
        new_lines.append(line)
with open('src/main/resources/messages_ar.properties', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("Double-punctuation fix applied.")

new_keys = {
    'src/main/resources/messages.properties': {
        'fragments_layout.tagline': '— every book has a spine, every spine tells you something.',
        'home.surprise.me': 'Surprise Me',
    },
    'src/main/resources/messages_fr.properties': {
        'fragments_layout.tagline': '— chaque livre a une reliure, et chaque reliure vous raconte quelque chose.',
        'home.surprise.me': 'Surprends-moi',
    },
    'src/main/resources/messages_ar.properties': {
        'fragments_layout.tagline': '— لكل كتاب عمود فقري، ولكل عمود فقري ما يخبرك به.',
        'home.surprise.me': 'فاجئني',
    },
}
for path, keys in new_keys.items():
    with open(path, 'a', encoding='utf-8') as f:
        f.write('\n')
        for k, v in keys.items():
            f.write(f'{k}={v}\n')
print("New keys appended to all three files. Done.")