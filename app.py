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
