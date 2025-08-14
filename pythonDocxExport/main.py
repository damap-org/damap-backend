from docxtpl import DocxTemplate

doc = DocxTemplate("Sample.docx")
context = {}
context["projectname"] = "SAMPLE PROJECT NAME"
context["acronym"] = "SPN"
context["grantid"] = "SPN-12345"
context["projectid"] = "SPN-2023-001"

context["projectnameText"] = "Sample Project Name"
context["M4"] = "Sample M4 Text"
context["sectionName"] = "Sample Section Name"

context["datamanager"] = "John Doe"
context["datamanagerInfo"] = "Data Manager Information"

context["producedDatasets"] = [
    {"cols": ["ID 1", "Title 1", "Type 1", "Format 1", "Volume 1", "Sensitive 1"]},
    {"cols": ["ID 2", "Title 2", "Type 2", "Format 2", "Volume 2", "Sensitive 2"]},
    {"cols": ["ID 3", "Title 3", "Type 3", "Format 3", "Volume 3", "Sensitive 3"]},
    {"cols": ["ID 4", "Title 4", "Type 4", "Format 4", "Volume 4", "Sensitive 4"]},
]

doc.render(context)
doc.save("generated_doc.docx")