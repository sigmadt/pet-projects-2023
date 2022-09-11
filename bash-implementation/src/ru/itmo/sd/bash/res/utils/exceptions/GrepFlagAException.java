package ru.itmo.sd.bash.res.utils.exceptions;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class GrepFlagAException implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            var givenFlag = Integer.parseInt(value);
            if (givenFlag < 0) {
                throw new ParameterException(String.format(
                        "Invalid flag for Grep command. \n %s should be : positive \n Given : negative \n",
                        name
                ));
            }

        } catch (NumberFormatException e) {
            throw new ParameterException(String.format(
                    "Invalid flag for Grep command. \n %s should be : positive integer \n Given : %s \n",
                    name, value
            ));
        }
    }
}
