FROM python:3.11-slim
WORKDIR /tests
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["pytest", "-v", "--tb=short", "--html=/report/test-report.html", "--self-contained-html", "--junitxml=/report/junit-report.xml"]
