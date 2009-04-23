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
    client.asserts.assertText(validator='You are not logged in.', id='welcome
