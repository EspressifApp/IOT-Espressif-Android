# the python file is just for the beginner to learn how to deal with xml using python

def TestMiniDom():	
#from xml.dom import minidom
#doc = minidom.parse("employees.xml")
	from xml.dom import minidom
	doc = minidom.parse("employees.xml")
	
	# get root element: <employees/>
	root = doc.documentElement

	# get all children elements: <employee> <employee/>
	employees = root.getElementsByTagName("employee")

	for employee in employees:
		print("----------------------------")
		# element name : employee
		print(employee.nodeName)
		# element xml content : <employee><name>windows</name><age>20</age></employee>
		# basically equal to to pretty xml function
		print(employee.toxml())
		
		nameNode = employee.getElementsByTagName("name")[0]
		print(nameNode.childNodes)
		print(nameNode.nodeName + ":" + nameNode.childNodes[0].nodeValue)
		ageNode = employee.getElementsByTagName("age")[0]
		print(ageNode.childNodes)
		print(ageNode.nodeName + ":" + ageNode.childNodes[0].nodeValue)
		print("############################")

	for n in employee.childNodes:
		print(n)

def GenerateXml():
	import xml.dom.minidom
	impl = xml.dom.minidom.getDOMImplementation()
	dom = impl.createDocument(None,'employees',None)
	root = dom.documentElement
	employee = dom.createElement('employee')
	root.appendChild(employee)

	nameE = dom.createElement('name')
	nameT = dom.createTextNode('linux')
	nameE.appendChild(nameT)
	employee.appendChild(nameE)

	ageE = dom.createElement('age')
	ageT = dom.createTextNode('30')
	ageE.appendChild(ageT)
	employee.appendChild(ageE)

#f=open('employee2.xml','w',encoding='utf-8')
	f=open('employees.xml','w')
	dom.writexml(f,addindent=' ',newl = '\n',encoding='utf-8')
	f.close()

GenerateXml()
TestMiniDom()
