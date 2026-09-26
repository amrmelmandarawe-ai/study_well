import streamlit as st
import google.generativeai as genai

# إعداد واجهة الموقع
st.title("تطبيق Study Well 📚")
st.write("أهلاً بك! اكتب أي سؤال وسيقوم الذكاء الاصطناعي بالإجابة عليك.")

# الحصول على الـ API Key من إعدادات Streamlit الآمنة
try:
    api_key = st.secrets["GEMINI_API_KEY"]
    genai.configure(api_key=api_key)

    # اختيار الموديل
    model = genai.GenerativeModel('gemini-pro')

    # مكان كتابة المستخدم
    user_input = st.text_input("اكتب سؤالك هنا:")

    if st.button("إرسال") and user_input:
        with st.spinner('جاري التفكير...'):
            response = model.generate_content(user_input)
            st.write(response.text)

except Exception as e:
    st.error("الرجاء إضافة API Key في إعدادات التطبيق.")
import streamlit as st
import google.generativeai as genai

# إعداد شكل الصفحة
st.set_page_config(page_title="Study Well", page_icon="📚")
st.title("تطبيق Study Well 📚")

try:
    # ربط المفتاح السري
    api_key = st.secrets["GEMINI_API_KEY"]
    genai.configure(api_key=api_key)

    # 💡 [مهم جداً] هنا تحط التغييرات والتعليمات اللي كتبتها في AI Studio
    my_instructions = "أنت مساعد ذكي للطلاب. أجب على الأسئلة بطريقة علمية، مبسطة، ومنظمة."

    # تجهيز الذكاء الاصطناعي بنفس إعداداتك
    model = genai.GenerativeModel(
        'gemini-1.5-flash',
        system_instruction=my_instructions
    )

    # تشغيل ذاكرة المحادثة عشان يفتكر الكلام زي AI Studio
    if "chat_session" not in st.session_state:
        st.session_state.chat_session = model.start_chat(history=[])

    # عرض الرسائل القديمة في الشاشة
    for message in st.session_state.chat_session.history:
        role = "user" if message.role == "user" else "assistant"
        with st.chat_message(role):
            st.markdown(message.parts[0].text)

    # مربع إدخال الكلام تحت
    if prompt := st.chat_input("اكتب رسالتك هنا..."):
        # عرض كلام المستخدم
        st.chat_message("user").markdown(prompt)
        # إرسال الكلام لجوجل وعرض الرد
        response = st.session_state.chat_session.send_message(prompt)
        st.chat_message("assistant").markdown(response.text)

except Exception as e:
    st.error("تأكد من إضافة GEMINI_API_KEY في إعدادات Streamlit.")
