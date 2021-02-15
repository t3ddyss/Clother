from flask_wtf import FlaskForm
from wtforms import PasswordField, BooleanField, SubmitField
from wtforms.validators import Regexp
from clother.utils import get_password_regex


class ResetPasswordForm(FlaskForm):
    password = PasswordField(label='Enter new password in the field below',
                             validators=[Regexp(regex=get_password_regex())],
                             id='password_input_field')
    show_password = BooleanField('Show password', id='show_password_checkbox')
    submit = SubmitField('Submit', id='submit_button')
