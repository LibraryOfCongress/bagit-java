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
    client.asserts.assertText(xpath="//div[@id='content']/p", 
        validator="Your username and password didn't match. Please try again.")


# NOTE: No test 1.7 because the "forgot info" link doesn't do anything yet.


def test_case_1_8():
    client = WindmillTestClient(__name__)
    client.click(id='id_username')
    client.type(text='jim', id='id_username')
    client.type(text='jim', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello,', id='welcome')
    client.asserts.assertText(validator='', link='jim')
    client.asserts.assertText(xpath="//div[@id='reports']/h3", validator='My Transfers')
    client.asserts.assertNode(link='Update User Info')
    client.click(link='Update User Info')
    client.waits.forPageLoad(timeout='20000')
    client.waits.forElement(timeout='8000', id='id_first_name')
    # Change his name
    client.click(id='id_first_name')
    client.asserts.assertValue(validator='Jim', id='id_first_name')
    client.asserts.assertValue(validator='Brown', id='id_last_name')
    client.asserts.assertValue(validator='jim@example.com', id='id_email')
    client.type(text='Jimbo', id='id_first_name')
    client.click(id='id_email')
    client.type(text='jimbo@example.com', id='id_email')
    client.click(name='submit_name')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.')

    # Change it back to how it was before
    client.click(link='Update User Info')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertValue(validator='Jimbo', id='id_first_name')
    client.click(id='id_first_name')
    client.type(text='Jim', id='id_first_name')
    client.click(id='id_email')
    client.type(text='jim@example.com', id='id_email')
    client.click(name='submit_name')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.')

    # Change the address
    client.click(link='Update User Info')
    client.waits.forPageLoad(timeout='20000')
    client.waits.forElement(timeout='8000', id='id_address')
    client.click(id='id_address')
    client.type(text='17 8th St., NY, NY', id='id_address')
    client.click(name='submit_contact')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.')
    client.click(link='Update User Info')
    client.waits.forElement(timeout='8000', id='id_address')
    client.click(id='id_address')
    client.asserts.assertValue(validator='17 8th St., NY, NY', 
        id='id_address')
    # Change it back
    client.click(id='id_address')
    client.type(text='17 8th St., New York, NY', id='id_address')
    client.click(xpath="//fieldset[@id='submit_contact']/input")
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.')
    client.click(link='Update User Info')
    client.waits.forElement(timeout='8000', id='id_address')
    client.click(id='id_address')
    client.asserts.assertValue(validator='17 8th St., New York, NY', 
        id='id_address')
    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertNode(id='welcome')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')

def test_case_1_9():
    client = WindmillTestClient(__name__)
    # Change the password
    client.click(id='id_username')
    client.type(text='jim', id='id_username')
    client.type(text='jim', id='id_password')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello,', id='welcome')
    client.click(link='Update User Info')
    client.waits.forPageLoad(timeout='20000')
    client.waits.forElement(timeout='8000', id='id_old_password')
    client.click(id='id_old_password')
    client.type(text='jim', id='id_old_password')
    client.type(text='jimbo', id='id_new_password1')
    client.type(text='jimbo', id='id_new_password2')
    client.click(name='submit_pass')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.')
    # Try logging in with new pass
    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.type(text='jim', id='id_username')
    client.type(text='jimbo', id='id_password')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')
    client.click(name='submit')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(validator='Hello', id='welcome')
    # Change it back, but incorrectly
    client.click(link='Update User Info')
    client.waits.forPageLoad(timeout='20000')
    client.type(text='jim', id='id_old_password')
    client.type(text='jim', id='id_new_password1')
    client.type(text='jim', id='id_new_password2')
    client.click(name='submit_pass')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Please check your changes and try again.')
    client.asserts.assertText(xpath="//div[@id='content']/h3[2]", 
        validator='Please re-enter and confirm your password information again.')
    # Now fix it back to the original for real
    client.click(id='id_old_password')
    client.type(text='jimbo', id='id_old_password')
    client.type(text='jim', id='id_new_password1')
    client.type(text='jim', id='id_new_password2')
    client.click(name='submit_pass')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertText(xpath="//div[@id='messages']/h3", 
        validator='Your information has been updated.') 

    client.click(link='Log out')
    client.waits.forPageLoad(timeout='20000')
    client.asserts.assertNode(id='welcome')
    client.asserts.assertText(validator='You are not logged in.', id='welcome')
