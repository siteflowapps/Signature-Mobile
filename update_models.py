with open("composeApp/src/commonMain/kotlin/com/siteflow/signature/ase/dashboard/data/OutletModels.kt", "r") as f:
    code = f.read()

code = code.replace("val distributorId: String", "val dmsId: String")
code = code.replace("distributorId = ", "dmsId = ")

with open("composeApp/src/commonMain/kotlin/com/siteflow/signature/ase/dashboard/data/OutletModels.kt", "w") as f:
    f.write(code)

print("Replaced distributorId with dmsId")
