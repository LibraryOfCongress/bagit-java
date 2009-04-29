from windmill.authoring import WindmillTestClient


def test_case_1_1():
    client = WindmillTestClient(__name__)
    client.asserts.assertText(validator='You are not logged in.', id='welcome')
    client.asserts.assertNode(link='Contact')
    client.asserts.assertNode(xpath="//div[@id='login']/h1")
    client.asserts.assertNode(link='Forgot login information?')


def test_case_1_2():
    client = WindmillTestClient(__name__)
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')
    client.click(id='id_username')
    client.type(text='justin', id='id_username')
    client.type(text='justin', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello, ', id='welcome')
    client.asserts.assertNode(link='justin')
    client.asserts.assertNode(link='Update User Info')
    client.asserts.assertNode(link='Log out')
    client.asserts.assertNode(link='Contact')
    client.asserts.assertNode(link='Go to Admin Site')
    client.asserts.assertText(xpath="//div[@id='reports']/h3", validator='My Transfers')
    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertNode(id='welcome')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')


def test_case_1_3():
    client = WindmillTestClient(__name__)
    client.click(id='id_username')
    client.type(text='michelleg', id='id_username')
    client.type(text='michelle', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello,', id='welcome')
    client.asserts.assertText(validator='', link='michelleg')
    client.asserts.assertText(xpath="//div[@id='reports']/h3", validator='My Transfers')
    client.asserts.assertNode(link='Update User Info')
    client.asserts.assertNode(link='Log out')
    client.asserts.assertNode(link='Contact')
    client.asserts.assertNode(link='Go to Admin Site')
    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')


def test_case_1_4():
    client = WindmillTestClient(__name__)
    client.click(id='id_username')
    client.type(text='jane', id='id_username')
    client.type(text='jane', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello,', id='welcome')
    client.asserts.assertText(validator='', link='jane')
    client.asserts.assertText(xpath="//div[@id='reports']/h3", validator='My Transfers')
    client.asserts.assertNode(link='Update User Info')
    client.asserts.assertNode(link='Log out')
    client.asserts.assertNode(link='Contact')
    client.asserts.assertNode(link='Create Network Transfer')
    client.asserts.assertNode(link='Create Shipment Transfer')
    client.asserts.assertNode(id='nettrans')
    client.asserts.assertNode(id='shiptrans')
    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')


# NOTE: No test 1.5 because prior tests already test logging out.


def test_case_1_6():
    client = WindmillTestClient(__name__)
    client.click(id='id_username')
    client.type(text='bob', id='id_username')
    client.type(text='bob', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')
    client.asserts.assertText(xpath="//div[@id='content']/p", validator="Your username and password didn't match. Please try again.")

