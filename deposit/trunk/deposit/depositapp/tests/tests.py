import os

from windmill.authoring import djangotest


class TestUI(djangotest.WindmillDjangoUnitTest):
    test_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)),
        'windmilltests')
    browser = 'firefox'
    test_port = 8089
